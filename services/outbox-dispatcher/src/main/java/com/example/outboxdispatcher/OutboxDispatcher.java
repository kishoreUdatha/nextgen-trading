package com.example.outboxdispatcher;

import com.example.common.events.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Periodically scans the outbox and publishes events to Kafka with retries.
 * Tip: Prefer putting @EnableScheduling on a @Configuration class.
 */
@Component
@EnableScheduling
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    // --- SQL ---
    private static final String SQL_SELECT_DUE_EVENTS = """
        SELECT id, topic, payload_json, attempts, type
        FROM event_outbox
        WHERE (status = 'NEW'
               OR (status = 'FAILED' AND (next_retry_at IS NULL OR next_retry_at <= now())))
        ORDER BY created_at ASC
        LIMIT ?
        FOR UPDATE SKIP LOCKED
        """;

    private static final String SQL_MARK_SENT = """
        UPDATE event_outbox
        SET status = 'SENT',
            published_at = ?,
            published_partition = ?,
            published_offset = ?,
            attempts = attempts + 1
        WHERE id = ?
        """;

    private static final String SQL_MARK_FAILED_WITH_RETRY = """
        UPDATE event_outbox
        SET status = 'FAILED',
            attempts = ?,
            next_retry_at = ?,
            last_error = ?
        WHERE id = ?
        """;

    private static final String SQL_MARK_FAILED_FINAL = """
        UPDATE event_outbox
        SET status = 'FAILED',
            attempts = ?,
            next_retry_at = NULL,
            last_error = ?
        WHERE id = ?
        """;

    // --- Deps + Config ---
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final SchemaValidator schemaValidator;

    private final int maxAttempts;
    private final long baseDelayMs;
    private final long jitterMs;
    private final int batchSize;
    private final String poisonSuffix;

    public OutboxDispatcher(JdbcTemplate jdbc,
                            KafkaTemplate<String, String> kafka,
                            @Value("${app.retry.maxAttempts:5}") int maxAttempts,
                            @Value("${app.retry.baseDelayMs:500}") long baseDelayMs,
                            @Value("${app.retry.jitterMs:250}") long jitterMs,
                            @Value("${app.outbox.batchSize:50}") int batchSize,
                            @Value("${app.kafka.poisonSuffix:.poison}") String poisonSuffix) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.schemaValidator = new SchemaValidator();
        this.maxAttempts = maxAttempts;
        this.baseDelayMs = baseDelayMs;
        this.jitterMs = jitterMs;
        this.batchSize = batchSize;
        this.poisonSuffix = poisonSuffix;
    }

    @Scheduled(fixedDelayString = "${app.outbox.pollMs:1000}")
    @Transactional
    public void dispatchTick() {
        List<OutboxRow> rows = fetchDueEvents(batchSize);
        if (rows.isEmpty()) {
            return;
        }
        log.info("outbox.tick batchSize={}", rows.size());

        for (OutboxRow row : rows) {
            MDC.put("outboxId", row.id().toString());
            try {
                dispatchOne(row);
            } catch (Exception ex) {
                // Safety net (should be handled inside dispatchOne)
                log.error("outbox.dispatch.unexpected id={} reason={}", row.id(), ex.toString(), ex);
            } finally {
                MDC.clear();
            }
        }
    }

    // --- Core ---

    private List<OutboxRow> fetchDueEvents(int limit) {
        return jdbc.query(SQL_SELECT_DUE_EVENTS,
                ps -> ps.setInt(1, limit),
                (rs, i) -> new OutboxRow(
                        (UUID) rs.getObject("id"),
                        rs.getString("topic"),
                        rs.getString("payload_json"),
                        rs.getInt("attempts"),
                        rs.getString("type")
                ));
    }

    private void dispatchOne(OutboxRow row) {
        // 1) Validate schema (final fail → mark + poison)
        try {
            String schema = mapTypeToSchema(row.type());
            schemaValidator.validate(schema, row.payloadJson());
        } catch (Exception validationEx) {
            log.warn("outbox.schema.invalid id={} type={} reason={}", row.id(), row.type(), validationEx.toString());
            markFailedFinal(row, validationEx);
            sendPoison(row);
            return;
        }

        // 2) Publish to Kafka
        try {
            var result = kafka.send(row.topic(), row.payloadJson()).get(); // need metadata
            var meta = result.getRecordMetadata();

            Metrics.SENT.increment();
            jdbc.update(SQL_MARK_SENT,
                    Timestamp.from(Instant.now()),
                    meta.partition(),
                    meta.offset(),
                    row.id());

            log.info("outbox.sent id={} topic={} partition={} offset={}",
                    row.id(), row.topic(), meta.partition(), meta.offset());

        } catch (Exception ex) {
            int nextAttempts = row.attempts() + 1;
            Metrics.FAILED.increment();

            if (nextAttempts >= maxAttempts) {
                log.error("outbox.failed.final id={} attempts={} reason={}", row.id(), nextAttempts, ex.toString(), ex);
                jdbc.update(SQL_MARK_FAILED_FINAL,
                        nextAttempts,
                        truncate(ex.toString(), 4000),
                        row.id());
                sendPoison(row);
            } else {
                long backoffMs = computeBackoffMs(nextAttempts);
                Instant nextRetryAt = Instant.now().plusMillis(backoffMs);

                log.warn("outbox.failed.retry id={} attempts={} nextRetryAt={} reason={}",
                        row.id(), nextAttempts, nextRetryAt, ex.toString());

                jdbc.update(SQL_MARK_FAILED_WITH_RETRY,
                        nextAttempts,
                        Timestamp.from(nextRetryAt),
                        truncate(ex.toString(), 4000),
                        row.id());
            }
        }
    }

    // --- Helpers ---

    private void markFailedFinal(OutboxRow row, Exception ex) {
        jdbc.update(SQL_MARK_FAILED_FINAL,
                row.attempts() + 1,
                truncate(ex.toString(), 4000),
                row.id());
        Metrics.FAILED.increment();
    }

    private void sendPoison(OutboxRow row) {
        try {
            kafka.send(row.topic() + poisonSuffix, row.payloadJson());
            log.warn("outbox.poison.sent id={} topic={}", row.id(), row.topic() + poisonSuffix);
        } catch (Exception poisonEx) {
            // Do not retry poison; only log
            log.error("outbox.poison.failed id={} reason={}", row.id(), poisonEx.toString(), poisonEx);
        }
    }

    private long computeBackoffMs(int attempts) {
        int capped = Math.min(attempts, 6);               // cap exponent growth
        long exp = (long) (baseDelayMs * Math.pow(2, capped));
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, jitterMs));
        return exp + jitter;
    }

    private String mapTypeToSchema(String type) {
        if (type == null) return "OutboxEvent.json";
        return switch (type) {
            case "OrderPlaced" -> "OrderPlaced.json";
            case "ExecRoute" -> "ExecRoute.json";
            case "TradeBooked" -> "TradeBooked.json";
            default -> "OutboxEvent.json";
        };
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /** Minimal row mapping for event_outbox table. */
    private record OutboxRow(UUID id, String topic, String payloadJson, int attempts, String type) {}
}

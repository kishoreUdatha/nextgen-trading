package com.example.oms.scheduler;

import com.example.oms.config.MarketSession;
import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Component
public class MarketDayExpiry {

    private static final Logger log = LoggerFactory.getLogger(MarketDayExpiry.class);

    private final OrderRepository repo;
    private final MarketSession session;
    private final ObjectMapper om = new ObjectMapper();

    public MarketDayExpiry(OrderRepository repo, MarketSession session) {
        this.repo = repo; this.session = session;
    }

    // Check every minute; align to IST close and skip holidays
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndExpire() {
        long t0 = System.nanoTime();

        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalDate todayIst = LocalDate.now(ist);
        LocalTime marketClose = LocalTime.parse(session.getCloseIst());
        ZonedDateTime nowIst = ZonedDateTime.now(ist);
        ZonedDateTime closeIst = ZonedDateTime.of(todayIst, marketClose, ist);

        log.debug("MarketDayExpiry tick: nowIST={}, closeIST={}, holidays={}",
                nowIst, closeIst, session.getHolidays());

        // Skip if holiday
        boolean isHoliday = session.getHolidays().stream()
                .anyMatch(d -> d.equals(todayIst.toString()));
        if (isHoliday) {
            log.info("Skipping expiry: {} is a market holiday (IST).", todayIst);
            return;
        }

        // Skip if before close
        if (nowIst.isBefore(closeIst)) {
            Duration toClose = Duration.between(nowIst, closeIst);
            log.debug("Skipping expiry: market not closed yet ({} min to close).",
                    Math.max(0, toClose.toMinutes()));
            return;
        }

        // Find candidates
        List<OrderEntity> openOrders = repo.findAll().stream()
                .filter(o -> "DAY".equals(o.getTif()))
                .filter(o -> List.of("NEW", "VALIDATED", "ROUTED", "PARTIALLY_FILLED").contains(o.getStatus()))
                .toList();

        log.info("Expiring DAY orders after close: candidates={}", openOrders.size());

        int expired = 0;
        for (OrderEntity e : openOrders) {
            try {
                log.info("Expiring order id={}, user={}, symbol={}, prevStatus={}",
                        e.getId(), e.getUserId(), e.getSymbol(), e.getStatus());

                e.setStatus(OrderStatus.EXPIRED);
                e.setUpdatedAt(Instant.now());
                repo.save(e);

                // Emit WS notification
                String msg = om.createObjectNode()
                        .put("orderId", e.getId().toString())
                        .put("status", "EXPIRED")
                        .toString();
                OrderStream.ORDER_UPDATES.tryEmitNext(msg);

                log.debug("Order expired & notified: id={}, updatedAt={}", e.getId(), e.getUpdatedAt());
                expired++;
            } catch (Exception ex) {
                log.error("Failed to expire order id={}: {}", e.getId(), ex.toString(), ex);
            }
        }

        long tookMs = (System.nanoTime() - t0) / 1_000_000;
        log.info("MarketDayExpiry run complete: expired={}, took={} ms", expired, tookMs);
    }
}

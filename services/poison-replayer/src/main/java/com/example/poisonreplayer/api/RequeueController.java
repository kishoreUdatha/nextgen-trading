package com.example.poisonreplayer.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/requeue", produces = MediaType.APPLICATION_JSON_VALUE)
public class RequeueController {

    private final KafkaTemplate<String, String> kafka;
    private final String prefix;

    public RequeueController(KafkaTemplate<String, String> kafka,
                             @Value("${app.topicPrefix:tp}") String prefix) {
        this.kafka = kafka;
        this.prefix = prefix;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> requeue(@RequestParam String poisonTopic,
                                             @RequestBody String payload) {
        // validate against inferred schema
        String schema = inferSchema(poisonTopic);
        new com.example.common.events.SchemaValidator().validate(schema, payload);

        // strip .poison suffix and re-publish
        String target = poisonTopic.endsWith(".poison")
                ? poisonTopic.substring(0, poisonTopic.length() - 7)
                : poisonTopic;

        kafka.send(target, payload);
        com.example.poisonreplayer.metrics.ReplayerMetrics.REQUEUED.increment();

        return Mono.just(Map.of("status", "requeued", "to", target));
    }

    private String inferSchema(String topic) {
        // (Optional) normalize without prefix if your topics are like "tp.exec.route.poison"
        // String t = topic.startsWith(prefix + ".") ? topic.substring(prefix.length() + 1) : topic;
        String t = topic; // keep simple if not needed

        if (t.contains("orders.placed"))  return "OrderPlaced.json";
        if (t.contains("exec.route"))     return "ExecRoute.json";
        if (t.contains("exec.reports"))   return "ExecReport.json";
        if (t.contains("trades.booked"))  return "TradeBooked.json";
        if (t.contains("risk.approved"))  return "RiskApproved.json";
        if (t.contains("risk.blocks"))    return "RiskBlocked.json";
        return "OutboxEvent.json";
    }
}

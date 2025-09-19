package com.example.oms.messaging;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.domain.OutboxEntity;
import com.example.oms.domain.OutboxRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderStream;
import com.example.oms.metrics.OmsMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.example.common.events.SchemaValidator;

@Component
public class ExecReportConsumer {
  private final OrderRepository orders;
  private final OutboxRepository outbox;
  private final ObjectMapper om;
  private final String prefix;
  private final OmsMetrics metrics;
  public ExecReportConsumer(OrderRepository orders,
                            OutboxRepository outbox,
                            ObjectMapper om, OmsMetrics metrics,
                            @Value("${app.topicPrefix:tp}") String prefix){
    this.orders=orders; this.outbox=outbox; this.om=om; this.metrics=metrics; this.prefix=prefix;
  }
    private final SchemaValidator validator = new SchemaValidator();

  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.exec.reports')}")
  @Transactional
  public void onExec(String payload) throws Exception {
    validator.validate("ExecReport.json", payload);
    JsonNode n = om.readTree(payload);
    UUID orderId = UUID.fromString(n.get("orderId").asText());
    Optional<OrderEntity> opt = orders.findById(orderId);
    if (opt.isEmpty()) return;
    OrderEntity e = opt.get();
    int delta = n.get("qty").asInt();
    e.setQty(Math.min(e.getQty(), e.getQty() + delta));
    String status = n.get("status").asText("FILLED");

    if ("PARTIAL".equals(status) || e.getQty() < e.getQty()) {
      e.setStatus(OrderStatus.PARTIALLY_FILLED);
      // IOC remainder auto-cancel
      if ("IOC".equals(e.getTif())) {
        e.setStatus(OrderStatus.CANCELLED);
        e.setUpdatedAt(Instant.now());
        orders.save(e);
          OrderStream.ORDER_UPDATES.tryEmitNext(
                  "{\"orderId\":\"" + e.getId() + "\",\"status\":\"PARTIALLY_FILLED\"}"
          );
          OrderStream.ORDER_UPDATES.tryEmitNext(
                  "{\"orderId\":\"" + e.getId() + "\",\"status\":\"CANCELLED\",\"reason\":\"IOC_REMAINDER\"}"
          );

          metrics.ordersCancelled.increment();
        return;
      }
    } else {
      e.setStatus(OrderStatus.FILLED);
      metrics.ordersFilled.increment();
      // Publish trades.booked
      OutboxEntity ox = new OutboxEntity();
      ox.setId(UUID.randomUUID());
      ox.setAggregateId(e.getId());
      ox.setTopic(prefix + ".trades.booked");
      ox.setType("TradeBooked");
      ox.setPayloadJson(om.createObjectNode()
        .put("orderId", e.getId().toString())
        .put("execId", n.get("execId").asText())
        .put("qty", n.get("qty").asInt())
        .put("price", n.get("price").asDouble()).toString());
      ox.setCreatedAt(Instant.now());
      outbox.save(ox);
    }
    e.setUpdatedAt(Instant.now());
    orders.save(e);
      String json = om.createObjectNode()
              .put("orderId", e.getId().toString())
              .put("status", String.valueOf(e.getStatus()))
              .toString();
      OrderStream.ORDER_UPDATES.tryEmitNext(json);

  }
}

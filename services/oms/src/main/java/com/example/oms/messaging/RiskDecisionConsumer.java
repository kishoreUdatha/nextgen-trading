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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RiskDecisionConsumer {

  private final OrderRepository orders;
  private final OutboxRepository outbox;
  private final ObjectMapper om;
  private final String prefix;
  private final OmsMetrics metrics;

  public RiskDecisionConsumer(OrderRepository orders, OutboxRepository outbox, ObjectMapper om,
                              OmsMetrics metrics,
                              @Value("${app.topicPrefix:tp}") String prefix){
    this.orders=orders; this.outbox=outbox; this.om=om; this.metrics=metrics; this.prefix=prefix;
  }

  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.risk.approved')}")
  @Transactional
  public void onApproved(String payload) throws Exception {
    JsonNode node = om.readTree(payload);
    UUID id = UUID.fromString(node.get("orderId").asText());
    Optional<OrderEntity> opt = orders.findById(id);
    if (opt.isEmpty()) return;
    OrderEntity e = opt.get();
    e.setStatus(OrderStatus.ROUTED); e.setUpdatedAt(Instant.now());
    orders.save(e);

    // Route to execution via outbox with full context
    OutboxEntity ox = new OutboxEntity();
    ox.setId(UUID.randomUUID());
    ox.setAggregateId(e.getId());
    ox.setTopic(prefix + ".exec.route");
    ox.setType("ExecRoute");
    String route = om.createObjectNode().put("orderId", e.getId().toString()).put("symbol", e.getSymbol()).put("qty", e.getQty()).put("tif", e.getTif().ordinal()).toString();
    new com.example.common.events.SchemaValidator().validate("ExecRoute.json", route);
    ox.setPayloadJson(route);
    ox.setCreatedAt(Instant.now());
    outbox.save(ox);

    ObjectMapper om = new ObjectMapper();
      ObjectNode n = om.createObjectNode()
              .put("orderId", e.getId().toString())
              .put("status", "ROUTED");
      OrderStream.ORDER_UPDATES.tryEmitNext(n.toString());
  }

  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.risk.blocks')}")
  @Transactional
  public void onBlocked(String payload) throws Exception {
    JsonNode node = om.readTree(payload);
    UUID id = UUID.fromString(node.get("orderId").asText());
    Optional<OrderEntity> opt = orders.findById(id);
    if (opt.isEmpty()) return;
    OrderEntity e = opt.get();
    e.setStatus(OrderStatus.REJECTED); e.setUpdatedAt(Instant.now());
    orders.save(e);
    metrics.ordersRejected.increment();
      ObjectNode n = om.createObjectNode()
              .put("orderId", e.getId().toString())
              .put("status", "REJECTED");
      OrderStream.ORDER_UPDATES.tryEmitNext(n.toString());
  }
}

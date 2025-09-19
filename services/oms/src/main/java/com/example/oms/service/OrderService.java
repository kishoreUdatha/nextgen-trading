package com.example.oms.service;


import com.example.common.domain.Enums;
import com.example.common.events.OrderPlacedEvent;
import com.example.common.events.SchemaValidator;
import com.example.oms.domain.*;
import com.example.oms.enums.OrderStatus;
import com.example.oms.enums.OrderType;
import com.example.oms.enums.Side;
import com.example.oms.enums.TimeInForce;
import com.example.oms.metrics.OmsMetrics;
import com.example.oms.utils.LogFmt;
import com.example.oms.ws.OrderEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OmsMetrics metrics;
    private final String topicPrefix;
    private final SchemaValidator schemaValidator = new SchemaValidator();
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        OutboxRepository outboxRepository,
                        ObjectMapper objectMapper,
                        OmsMetrics metrics,
                        @Value("${app.topicPrefix:tp}") String topicPrefix,
                        OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        this.topicPrefix = topicPrefix;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Preferred type-safe API (use enums).
     */
    @Transactional
    public OrderEntity place(String userId,
                             String symbol,
                             String segment,
                             Side side,
                             int quantity,
                             OrderType orderType,
                             Double limitPrice,
                             Double triggerPrice,
                             TimeInForce timeInForce) {

        final long startNs = System.nanoTime();
        final Instant now = Instant.now();

        final UUID orderId = UUID.randomUUID();
        final String clientOrderId = UUID.randomUUID().toString();

        MDC.put("orderId", orderId.toString());
        MDC.put("userId", userId);
        try {
            log.info("order.place.start {}", LogFmt.kv(
                    "userId", userId, "symbol", symbol, "segment", segment,
                    "side", side, "qty", quantity, "type", orderType, "tif", timeInForce));

            // Persist order
            OrderEntity order = new OrderEntity();
            order.setId(orderId);
            order.setClientOrderId(clientOrderId);
            order.setUserId(userId);
            order.setSymbol(symbol);
            order.setSegment(segment);
            order.setSide(side);
            order.setQty(quantity);
            order.setPrice(limitPrice);
            order.setTriggerPrice(triggerPrice);
            order.setOrderType(orderType);
            order.setTif(timeInForce);
            order.setStatus(OrderStatus.NEW);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            orderRepository.save(order);
            log.debug("order.persisted {}", LogFmt.kv("orderId", orderId, "clientOrderId", clientOrderId));

            // Event + Outbox (extracted)
            writeOrderPlacedOutbox(orderId, userId, symbol, quantity, now);

            // Metrics

            metrics.ordersPlaced.increment();
            log.debug("metric.incremented {}", LogFmt.kv("name", "ordersPlaced", "delta", 1));

            // Fire-and-forget WS
            orderEventPublisher.publishUpdate(
                    "{\"orderId\":\"" + orderId + "\",\"status\":\"NEW\"}");
            log.debug("ws.emitted {}", LogFmt.kv("orderId", orderId, "status", OrderStatus.NEW));

            long tookMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("order.place.success {}", LogFmt.kv("orderId", orderId, "tookMs", tookMs));

            return order;

        } finally {
            MDC.clear();
        }
    }

    /**
     * Backward-compatible adapter if some controllers still send strings.
     * You can delete this once all callers use enums.
     */
    @Transactional
    public OrderEntity place(String userId,
                             String symbol,
                             String segment,
                             String side,
                             int quantity,
                             String orderType,
                             Double limitPrice,
                             Double triggerPrice,
                             String tif) {
        return place(userId, symbol, segment,
                Side.parse(side),
                quantity,
                OrderType.parse(orderType),
                limitPrice,
                triggerPrice,
                TimeInForce.parse(tif));
    }

    private void writeOrderPlacedOutbox(UUID orderId,
                                        String userId,
                                        String symbol,
                                        int quantity,
                                        Instant createdAt) {
        try {
            OrderPlacedEvent event = new OrderPlacedEvent(orderId.toString(), userId, symbol, quantity);
            String payloadJson = objectMapper.writeValueAsString(event);
            schemaValidator.validate("OrderPlaced.json", payloadJson);
            log.debug("event.validated {}", LogFmt.kv("type", "OrderPlaced", "bytes", payloadJson.length()));

            OutboxEntity outbox = new OutboxEntity();
            outbox.setId(UUID.randomUUID());
            outbox.setAggregateId(orderId);
            outbox.setTopic(topicPrefix + ".orders.placed");
            outbox.setType("OrderPlaced");
            outbox.setPayloadJson(payloadJson);
            outbox.setCreatedAt(createdAt);

            outboxRepository.save(outbox);
            log.info("outbox.saved {}", LogFmt.kv("outboxId", outbox.getId(), "topic", outbox.getTopic(), "aggregateId", orderId));

        } catch (Exception ex) {
            log.error("outbox.prepare.failed {}", LogFmt.kv("orderId", orderId, "reason", ex.toString()), ex);
            throw new RuntimeException("Failed to prepare outbox/event", ex);
        }
    }
}


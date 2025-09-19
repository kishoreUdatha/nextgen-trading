package com.example.common.domain.events;

import com.example.common.domain.Enums.*;

import java.time.Instant;
import java.util.UUID;

public class Events {

    public record OrderPlaced(String eventId, String orderId, String userId,
                              String symbol, Segment segment, Side side,
                              int qty, Double price, Double triggerPrice,
                              OrderType orderType, TIF tif, Instant ts) {
        public OrderPlaced(String orderId, String userId, String symbol, Segment segment, Side side,
                           int qty, Double price, Double triggerPrice, OrderType orderType, TIF tif) {
            this(UUID.randomUUID().toString(), orderId, userId, symbol, segment, side, qty, price, triggerPrice, orderType, tif, Instant.now());
        }
    }

    public record RiskApproved(String eventId, String orderId, Instant ts) {
        public RiskApproved(String orderId) {
            this(UUID.randomUUID().toString(), orderId, Instant.now());
        }
    }

    public record RiskBlocked(String eventId, String orderId, String reason, Instant ts) {
        public RiskBlocked(String orderId, String reason) {
            this(UUID.randomUUID().toString(), orderId, reason, Instant.now());
        }
    }

    public record ExecReport(String eventId, String orderId, String execId, int qty, double price, String status,
                             Instant ts) {
        public ExecReport(String orderId, String execId, int qty, double price, String status) {
            this(UUID.randomUUID().toString(), orderId, execId, qty, price, status, Instant.now());
        }
    }

    public record TradeBooked(String eventId, String orderId, String execId, int qty, double price, Instant ts) {
        public TradeBooked(String orderId, String execId, int qty, double price) {
            this(UUID.randomUUID().toString(), orderId, execId, qty, price, Instant.now());
        }
    }
}

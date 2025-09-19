package com.example.oms.api;

import com.example.oms.request.PlaceOrderRequest;
import com.example.oms.response.OrderResponse;
import com.example.oms.utils.LogFmt;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.example.oms.service.OrderService;
import com.example.oms.domain.OrderEntity;
import com.example.oms.config.MarketSession;

import java.net.URI;
import java.time.*;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequestMapping(path="/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderApiController {

    private static final Logger log = LoggerFactory.getLogger(OrderApiController.class);

    private final OrderService orderService;
    private final MarketSession marketSession;

    public OrderApiController(OrderService orderService, MarketSession marketSession) {
        this.orderService = orderService;
        this.marketSession = marketSession;
    }

    private boolean isTradingOpen() {
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(ist);

        if (marketSession.getHolidays().contains(today.toString())) {
            log.warn("market.closed {}", LogFmt.kv("date", today, "reason", "HOLIDAY"));
            return false;
        }

        LocalTime now = LocalTime.now(ist);
        LocalTime open = LocalTime.parse(marketSession.getOpenIst());
        LocalTime close = LocalTime.parse(marketSession.getCloseIst());

        boolean openFlag = !now.isBefore(open) && !now.isAfter(close);
        if (!openFlag) {
            log.warn("market.closed {}", LogFmt.kv("now", now, "open", open, "close", close));
        }
        return openFlag;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> placeOrder(
            @RequestBody PlaceOrderRequest request,
            @RequestHeader(value="Idempotency-Key", required=false) String idempotencyKey) {

        log.info("order.api.request {}", LogFmt.kv(
                "symbol", request.symbol(),
                "segment", request.segment(),
                "side", request.orderSide(),
                "qty", request.quantity(),
                "type", request.orderType(),
                "tif", request.timeInForce(),
                "idempotencyKey", idempotencyKey
        ));

        if (isTradingOpen()) {
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of("error", "MARKET_CLOSED"));
        }

        try {
            OrderEntity entity = orderService.place(
                    "demo-user",
                    request.symbol(),
                    request.segment(),
                    request.orderSide(),
                    request.quantity(),
                    request.orderType(),
                    request.limitPrice(),
                    request.triggerPrice(),
                    request.timeInForce()
            );

            log.info("order.api.success {}", LogFmt.kv(
                    "orderId", entity.getId(),
                    "status", entity.getStatus(),
                    "userId", entity.getUserId()
            ));

            return ResponseEntity
                    .created(URI.create("/api/v1/orders/" + entity.getId()))
                    .body(new OrderResponse(
                            entity.getId().toString(),
                            entity.getStatus().toString(),
                            entity.getCreatedAt()
                    ));
        } catch (Exception ex) {
            log.error("order.api.failed {}", LogFmt.kv(
                    "symbol", request.symbol(),
                    "side", request.orderSide(),
                    "qty", request.quantity(),
                    "reason", ex.toString()
            ), ex);

            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "ORDER_FAILED"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String id) {
        log.info("order.api.get {}", LogFmt.kv("orderId", id));
        // TODO: Replace with DB lookup
        return new ResponseEntity<>(new OrderResponse(id, "NEW", Instant.now()), HttpStatus.OK);
    }
}

package com.example.oms.api;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderEventPublisher;
import com.example.oms.ws.WebSocketConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

record ModifyOrderRequest(@Min(1) Integer qty, Double price) {}

@RestController
@RequestMapping(path = "/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class ModifyCancelApi {

    private final OrderRepository repo;
    private final OrderEventPublisher  publisher;

    public ModifyCancelApi(OrderRepository repo, OrderEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @PostMapping("/{id}/modify")
    @Transactional
    public ResponseEntity<String> modify(@PathVariable String id, @RequestBody ModifyOrderRequest req) {
        UUID oid = UUID.fromString(id);
        Optional<OrderEntity> opt = repo.findById(oid);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderEntity e = opt.get();
        if (e.getStatus().equals("FILLED") || e.getStatus().equals("CANCELLED") || e.getStatus().equals("REJECTED")) {
            return ResponseEntity.status(409).body("{\"error\":\"IMMUTABLE_STATE\"}");
        }

        if (req.qty() != null) e.setQty(req.qty());
        if (req.price() != null) e.setPrice(req.price());
        e.setUpdatedAt(Instant.now());
        repo.save(e);

        publisher.publishUpdate(
                String.format("{\"orderId\":\"%s\",\"status\":\"MODIFIED\"}", e.getId())
        );

        return ResponseEntity.ok("{\"status\":\"OK\"}");
    }

    @PostMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<String> cancel(@PathVariable String id) {
        UUID oid = UUID.fromString(id);
        Optional<OrderEntity> opt = repo.findById(oid);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderEntity e = opt.get();
        if (e.getStatus().equals("FILLED") || e.getStatus().equals("CANCELLED")) {
            return ResponseEntity.status(409).body("{\"error\":\"IMMUTABLE_STATE\"}");
        }

        e.setStatus(OrderStatus.CANCELLED);
        e.setUpdatedAt(Instant.now());
        repo.save(e);

        ObjectMapper om = new ObjectMapper();
        ObjectNode n = om.createObjectNode()
                .put("orderId", e.getId().toString())
                .put("status", "CANCELLED");

        publisher.publishUpdate(n.toString());

        return ResponseEntity.ok("{\"status\":\"CANCELLED\"}");
    }
}

package com.example.oms.api;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

record ModifyOrderRequest(@Min(1) Integer qty, Double price){}

@RestController
@RequestMapping(path="/api/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class ModifyCancelApi {

  private final OrderRepository repo;
  public ModifyCancelApi(OrderRepository repo){ this.repo=repo; }

  @PostMapping("/{id}/modify")
  @Transactional
  public Mono<ResponseEntity<String>> modify(@PathVariable String id, @RequestBody ModifyOrderRequest req){
    UUID oid = UUID.fromString(id);
    Optional<OrderEntity> opt = repo.findById(oid);
    if (opt.isEmpty()) return Mono.just(ResponseEntity.notFound().build());
    OrderEntity e = opt.get();
    if (e.getStatus().equals("FILLED") || e.getStatus().equals("CANCELLED") || e.getStatus().equals("REJECTED")){
      return Mono.just(ResponseEntity.status(409).body("{\"error\":\"IMMUTABLE_STATE\"}"));
    }
    if (req.qty()!=null) e.setQty(req.qty());
    if (req.price()!=null) e.setPrice(req.price());
    e.setUpdatedAt(Instant.now());
    repo.save(e);
      OrderStream.ORDER_UPDATES.tryEmitNext(
              String.format("{\"orderId\":\"%s\",\"status\":\"MODIFIED\"}", e.getId()));
    return Mono.just(ResponseEntity.ok("{\"status\":\"OK\"}"));
  }

  @PostMapping("/{id}/cancel")
  @Transactional
  public Mono<ResponseEntity<String>> cancel(@PathVariable String id){
    UUID oid = UUID.fromString(id);
    Optional<OrderEntity> opt = repo.findById(oid);
    if (opt.isEmpty()) return Mono.just(ResponseEntity.notFound().build());
    OrderEntity e = opt.get();
    if (e.getStatus().equals("FILLED") || e.getStatus().equals("CANCELLED")){
      return Mono.just(ResponseEntity.status(409).body("{\"error\":\"IMMUTABLE_STATE\"}"));
    }
    e.setStatus(OrderStatus.CANCELLED); e.setUpdatedAt(Instant.now());
    repo.save(e);
      ObjectMapper om = new ObjectMapper();
      ObjectNode n = om.createObjectNode()
              .put("orderId", e.getId().toString())
              .put("status", "CANCELLED");
      OrderStream.ORDER_UPDATES.tryEmitNext(n.toString());

    return Mono.just(ResponseEntity.ok("{\"status\":\"CANCELLED\"}"));
  }
}

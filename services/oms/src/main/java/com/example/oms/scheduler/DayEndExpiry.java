package com.example.oms.scheduler;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class DayEndExpiry {

  private final OrderRepository repo;
  public DayEndExpiry(OrderRepository repo){ this.repo=repo; }

  // Run every minute; a real system would align to exchange schedule. Here we expire DAY orders placed before now.
  @Scheduled(cron = "${app.oms.day-end-cron:0 0 10 * * *}")
  @Transactional
  public void expireDayOrders(){
    List<OrderEntity> dayOpen = repo.findAll().stream()
      .filter(o -> "DAY".equals(o.getTif()))
      .filter(o -> ("NEW".equals(o.getStatus()) || "VALIDATED".equals(o.getStatus()) || "ROUTED".equals(o.getStatus())))
      .toList();
    for (OrderEntity e : dayOpen){
      e.setStatus(OrderStatus.EXPIRED);
      e.setUpdatedAt(Instant.now());
      repo.save(e);
      ObjectMapper om = new ObjectMapper();
        ObjectNode n = om.createObjectNode()
                .put("orderId", e.getId().toString())
                .put("status", "EXPIRED");
        OrderStream.ORDER_UPDATES.tryEmitNext(n.toString());
    }
  }
}

package com.example.oms.scheduler;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.List;

@Component
public class IocCanceller {

  private final OrderRepository repo;
  private final long timeoutMs;
  private final OrderEventPublisher orderEventPublisher;

  public IocCanceller(OrderRepository repo,
                      @Value("${app.oms.ioc-timeout-ms:2000}") long timeoutMs,
                      OrderEventPublisher orderEventPublisher) {
    this.repo=repo; this.timeoutMs=timeoutMs;
    this.orderEventPublisher=orderEventPublisher;
  }

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void sweep(){
    Instant cutoff = Instant.now().minusMillis(timeoutMs);
    List<OrderEntity> candidates = repo.findAll().stream()
      .filter(o -> "IOC".equals(o.getTif()))
      .filter(o -> ("NEW".equals(o.getStatus()) || "VALIDATED".equals(o.getStatus()) || "ROUTED".equals(o.getStatus())))
      .filter(o -> o.getCreatedAt().isBefore(cutoff))
      .toList();
    for (OrderEntity e : candidates){
      if (e.getQty() < e.getQty()){
        e.setStatus(OrderStatus.CANCELLED);
        e.setUpdatedAt(Instant.now());
        repo.save(e);
          orderEventPublisher.publishUpdate(
                  "{\"orderId\":\"" + e.getId() + "\",\"status\":\"CANCELLED\",\"reason\":\"IOC_TIMEOUT\"}"
          );
      }
    }
  }
}

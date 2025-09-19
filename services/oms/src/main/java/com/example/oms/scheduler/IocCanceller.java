package com.example.oms.scheduler;

import com.example.oms.domain.OrderEntity;
import com.example.oms.domain.OrderRepository;
import com.example.oms.enums.OrderStatus;
import com.example.oms.ws.OrderStream;
import jakarta.persistence.EntityManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

@Component
public class IocCanceller {

  private final OrderRepository repo;
  private final long timeoutMs;

  public IocCanceller(OrderRepository repo, @Value("${app.oms.ioc-timeout-ms:2000}") long timeoutMs){
    this.repo=repo; this.timeoutMs=timeoutMs;
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
          OrderStream.ORDER_UPDATES.tryEmitNext(
                  "{\"orderId\":\"" + e.getId() + "\",\"status\":\"CANCELLED\",\"reason\":\"IOC_TIMEOUT\"}"
          );
      }
    }
  }
}

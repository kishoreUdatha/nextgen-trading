package com.example.oms.messaging;

import com.example.oms.domain.OutboxEntity;
import com.example.oms.domain.OutboxRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {
  private final OutboxRepository repo;
  private final KafkaTemplate<String, String> kafka;
  public OutboxPublisher(OutboxRepository repo, KafkaTemplate<String,String> kafka){ this.repo=repo; this.kafka=kafka; }

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void publishBatch(){
    List<OutboxEntity> batch = repo.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
    for (OutboxEntity e : batch){
      kafka.executeInTransaction(kt -> {
        kt.send(e.getTopic(), e.getPayloadJson());
        return true;
      });
      e.setPublishedAt(Instant.now());
    }
    // JPA flush at txn end
  }
}

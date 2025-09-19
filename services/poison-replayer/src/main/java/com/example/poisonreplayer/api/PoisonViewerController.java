package com.example.poisonreplayer.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping(path="/api/v1/poison", produces = MediaType.APPLICATION_JSON_VALUE)
public class PoisonViewerController {
  private final String bootstrap;
  private final KafkaTemplate<String,String> kafka;
  public PoisonViewerController(@Value("${spring.kafka.bootstrap-servers}") String bootstrap,
                                KafkaTemplate<String,String> kafka){
    this.bootstrap=bootstrap; this.kafka=kafka;
  }

  @GetMapping
  public Mono<List<String>> tail(@RequestParam String topic, @RequestParam(defaultValue="50") int n){
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "poison-viewer-"+UUID.randomUUID());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    KafkaConsumer<String,String> c = new KafkaConsumer<>(props);
    c.subscribe(Collections.singletonList(topic));
    // assign to end, then seek back n
    c.poll(Duration.ofMillis(200));
    var parts = c.assignment();
    parts.forEach(p -> {
      long end = c.endOffsets(Collections.singleton(p)).get(p);
      long start = Math.max(0, end - n);
      c.seek(p, start);
    });
    List<String> result = new ArrayList<>();
    ConsumerRecords<String,String> recs = c.poll(Duration.ofSeconds(2));
    recs.forEach(r -> result.add(r.value()));
    c.close();
    return Mono.just(result);
  }

  record RequeueBatch(String topic, List<String> payloads) {}

  @PostMapping("/bulk-requeue")
  public Mono<Map<String,Object>> bulk(@RequestBody RequeueBatch batch){
    String target = batch.topic().endsWith(".poison") ? batch.topic().substring(0, batch.topic().length()-7) : batch.topic();
    int sent = 0;
    for (String p: batch.payloads()){
      kafka.send(target, p);
      com.example.poisonreplayer.metrics.ReplayerMetrics.REQUEUED.increment();
      sent++;
    }
    return Mono.just(Map.of("status","ok","requeued",sent,"to",target));
  }
}

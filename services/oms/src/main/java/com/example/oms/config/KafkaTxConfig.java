package com.example.oms.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTxConfig {

  @Bean
  public ProducerFactory<String, String> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrap,
                                                         @Value("${spring.application.name:oms}") String appName) {
    Map<String,Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, appName + "-txid");
    DefaultKafkaProducerFactory<String,String> pf = new DefaultKafkaProducerFactory<>(props);
    pf.setTransactionIdPrefix(appName + "-txid-");
    return pf;
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String,String> pf){
    KafkaTemplate<String,String> kt = new KafkaTemplate<>(pf);
    kt.setObservationEnabled(true);
    return kt;
  }

  @Bean(name = "kafkaTransactionManager")
  public KafkaTransactionManager<String,String> kafkaTransactionManager(ProducerFactory<String,String> pf){
    KafkaTransactionManager<String,String> tm = new KafkaTransactionManager<>(pf);
    return tm;
  }
}

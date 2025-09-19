package com.example.exec.adapter.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class Router {
  private final ObjectMapper om;
  private final KafkaTemplate<String,String> kafka;
  private final String prefix;
  private final JdbcTemplate jdbc;

  public Router(ObjectMapper om, KafkaTemplate<String,String> kafka, JdbcTemplate jdbc, @Value("${app.topicPrefix:tp}") String prefix){
    this.om=om; this.kafka=kafka; this.prefix=prefix; this.jdbc=jdbc;
  }

  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.exec.route')}")
  public void onRoute(String payload) throws Exception {
    JsonNode n = om.readTree(payload);
    String orderId = n.get("orderId").asText();
    int qty = n.has("qty") ? n.get("qty").asInt() : 1;
    String tif = n.has("tif") ? n.get("tif").asText() : "DAY";
    String dedupeKey = "route:" + orderId;
    if (exists(dedupeKey)) return; // idempotent
    tryInsert(dedupeKey);

    if ("IOC".equals(tif) && qty > 1) {
      // Emit a partial fill of 1 unit
      String partial = om.createObjectNode().put("orderId", orderId)
        .put("execId", UUID.randomUUID().toString())
        .put("status","PARTIAL")
        .put("qty", 1).put("price", 100.0).toString();
      new com.example.common.events.SchemaValidator().validate("ExecReport.json", partial);
    kafka.send(prefix + ".exec.reports", partial);
      // Do not fill the rest; OMS will auto-cancel the remainder for IOC
      return;
    }

    // Full fill default
    String execReport = om.createObjectNode().put("orderId", orderId)
      .put("execId", UUID.randomUUID().toString())
      .put("status","FILLED").put("qty", qty).put("price", 100.0).toString();
    new com.example.common.events.SchemaValidator().validate("ExecReport.json", execReport);
    kafka.send(prefix + ".exec.reports", execReport);
  }

  private boolean exists(String key){
    Integer cnt = jdbc.queryForObject("SELECT COUNT(1) FROM processed_keys WHERE key = ?", Integer.class, key);
    return cnt != null && cnt > 0;
  }
  private void tryInsert(String key){
    try { jdbc.update("INSERT INTO processed_keys(key) VALUES (?)", key); }
    catch (Exception ignored){}
  }
}

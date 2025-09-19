package com.example.risk.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.common.events.SchemaValidator;
@Component

public class RiskConsumer {
  public static final java.util.Deque<String> RECENT_BLOCKS = new java.util.concurrent.ConcurrentLinkedDeque<>();
  private final ObjectMapper om;
  private final KafkaTemplate<String, String> kafka;
  private final String prefix;
  private final WebClient mdClient;
  private final WebClient pfClient;

  private final SchemaValidator validator = new SchemaValidator();

  public RiskConsumer(ObjectMapper om, KafkaTemplate<String,String> kafka,
                      @Value("${app.topicPrefix:tp}") String prefix,
                      @Value("${app.mdBaseUrl}") String mdBaseUrl,
                      @Value("${app.portfolioBaseUrl:http://portfolio:8083}") String pfBaseUrl){
    this.om=om; this.kafka=kafka; this.prefix=prefix;
    this.mdClient=WebClient.create(mdBaseUrl);
    this.pfClient=WebClient.create(pfBaseUrl);
  }

  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.orders.placed')}")
  public void onOrderPlaced(ConsumerRecord<String, String> rec) throws Exception {
    validator.validate("OrderPlaced.json", rec.value());
    JsonNode node = om.readTree(rec.value());
    String orderId = node.get("orderId").asText();
    String userId = node.get("userId").asText("");
    String symbol = node.get("symbol").asText();
    int qty = node.get("qty").asInt();
    String side = node.get("side").asText();
    String orderType = node.get("orderType").asText();
    double price = node.hasNonNull("price") ? node.get("price").asDouble() :
      mdClient.get().uri("/api/v1/ltp/{symbol}", symbol).retrieve().bodyToMono(JsonNode.class).block().get("ltp").asDouble();

    // Price-band check
    JsonNode bands = mdClient.get().uri("/api/v1/price-bands/{symbol}", symbol)
      .retrieve().bodyToMono(JsonNode.class).block();
    double lower = bands.get("lower").asDouble();
    double upper = bands.get("upper").asDouble();
    if (!orderType.equals("MARKET")) {
      if (price < lower || price > upper) {
        String blocked = om.createObjectNode().put("orderId", orderId).put("reason","PRICE_BAND").toString();
        new com.example.common.events.SchemaValidator().validate("RiskBlocked.json", blocked);
        RECENT_BLOCKS.add(blocked); while (RECENT_BLOCKS.size()>200) RECENT_BLOCKS.pollFirst();
    kafka.send(prefix + ".risk.blocks", blocked);
        return;
      }
    }

    // Margin/Exposure (simplified): SPAN=12%, Exposure=5%
    double notional = price * qty;
    double spanReq = notional * 0.12;
    double expoReq = notional * 0.05;
    double required = Math.max(spanReq, expoReq);

    // Wallet balance
    double balance = pfClient.get().uri("/api/v1/wallet/{userId}", userId.isBlank()?"demo-user":userId)
      .retrieve().bodyToMono(JsonNode.class).block().get("balance").asDouble();

    if (balance < required){
      kafka.send(prefix + ".risk.blocks", om.createObjectNode()
        .put("orderId", orderId).put("reason","INSUFFICIENT_MARGIN").toString());
      return;
    }

    String approved = om.createObjectNode().put("orderId", orderId).toString();
    new com.example.common.events.SchemaValidator().validate("RiskApproved.json", approved);
    kafka.send(prefix + ".risk.approved", approved);
  }
}

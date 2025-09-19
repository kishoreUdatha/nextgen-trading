package com.example.portfolio.messaging;

import com.example.portfolio.domain.Position;
import com.example.portfolio.domain.PositionRepo;
import com.example.portfolio.ws.PositionStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.common.events.SchemaValidator;
@Component

public class TradeConsumer {
  private final ObjectMapper om;
  private final PositionRepo repo;
  private final String prefix;
  public TradeConsumer(ObjectMapper om, PositionRepo repo, @Value("${app.topicPrefix:tp}") String prefix){
    this.om=om; this.repo=repo; this.prefix=prefix;
  }

  private final SchemaValidator validator = new SchemaValidator();
  @KafkaListener(topics = "#{T(java.util.List).of('${app.topicPrefix:tp}.trades.booked')}")
  @Transactional
  public void onTrade(String payload) throws Exception {
    validator.validate("TradeBooked.json", payload);
    JsonNode n = om.readTree(payload);
    int qty = n.get("qty").asInt();
    double price = n.get("price").asDouble();
    String userId = "demo-user";
    String symbol = "NIFTY";
    Position pos = repo.findByUserIdAndSymbol(userId, symbol).orElseGet(() -> {
      Position p = new Position(); p.setUserId(userId); p.setSymbol(symbol); p.setNetQty(0); p.setAvgPrice(0.0); return p;
    });
    int newQty = pos.getNetQty() + qty;
    double newAvg = (pos.getAvgPrice()*pos.getNetQty() + price*qty) / Math.max(1, newQty);
    pos.setNetQty(newQty); pos.setAvgPrice(newAvg);
    repo.save(pos);
    ObjectMapper om = new ObjectMapper();
      ObjectNode json = om.createObjectNode()
              .put("userId", userId)
              .put("symbol", symbol)
              .put("netQty", newQty)     // int/long becomes JSON number
              .put("avgPrice", newAvg);  // double/BigDecimal -> JSON number
      PositionStream.POS_UPDATES.tryEmitNext(json.toString());
      }
}

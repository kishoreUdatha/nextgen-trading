package com.example.oms.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "event_outbox", schema = "oms")
public class OutboxEntity {
  @Id private UUID id;
  @Column(name="aggregate_id", nullable=false) private UUID aggregateId;
  @Column(nullable=false) private String topic;
  @Column(nullable=false) private String type;
  @Column(name="payload_json", nullable=false, columnDefinition="text") private String payloadJson;
  @Column(name="headers_json", columnDefinition="text") private String headersJson;
  @Column(name="created_at", nullable=false) private Instant createdAt;
  @Column(name="published_at") private Instant publishedAt;
  public UUID getId(){ return id; } public void setId(UUID id){ this.id=id; }
  public UUID getAggregateId(){ return aggregateId; } public void setAggregateId(UUID a){ this.aggregateId=a; }
  public String getTopic(){ return topic; } public void setTopic(String t){ this.topic=t; }
  public String getType(){ return type; } public void setType(String t){ this.type=t; }
  public String getPayloadJson(){ return payloadJson; } public void setPayloadJson(String p){ this.payloadJson=p; }
  public String getHeadersJson(){ return headersJson; } public void setHeadersJson(String h){ this.headersJson=h; }
  public Instant getCreatedAt(){ return createdAt; } public void setCreatedAt(Instant c){ this.createdAt=c; }
  public Instant getPublishedAt(){ return publishedAt; } public void setPublishedAt(Instant p){ this.publishedAt=p; }
}

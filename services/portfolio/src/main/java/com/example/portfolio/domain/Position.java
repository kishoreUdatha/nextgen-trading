package com.example.portfolio.domain;

import jakarta.persistence.*;

@Entity @Table(name="positions")
public class Position {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="user_id", nullable=false)
  private String userId;

  @Column(nullable=false)
  private String symbol;

  @Column(name="net_qty", nullable=false)
  private int netQty;

  @Column(name="avg_price", nullable=false)
  private double avgPrice;

  public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
  public String getUserId(){ return userId; } public void setUserId(String u){ this.userId=u; }
  public String getSymbol(){ return symbol; } public void setSymbol(String s){ this.symbol=s; }
  public int getNetQty(){ return netQty; } public void setNetQty(int q){ this.netQty=q; }
  public double getAvgPrice(){ return avgPrice; } public void setAvgPrice(double p){ this.avgPrice=p; }
}

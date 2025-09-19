package com.example.oms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.market")
public class MarketSession {
  /** Trading hours in IST (HH:mm) */
  private String openIst = "09:15";
  private String closeIst = "15:30";
  /** Holidays as ISO-8601 dates (e.g., 2025-01-26) */
  private List<String> holidays = List.of();
  public String getOpenIst(){ return openIst; } public void setOpenIst(String s){ this.openIst=s; }
  public String getCloseIst(){ return closeIst; } public void setCloseIst(String s){ this.closeIst=s; }
  public List<String> getHolidays(){ return holidays; } public void setHolidays(List<String> h){ this.holidays=h; }
}

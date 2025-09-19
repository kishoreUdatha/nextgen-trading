package com.example.oms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.oms")
public class OmsSettings {
  /** milliseconds to wait before auto-cancelling IOC orders that are not fully filled */
  private long iocTimeoutMs = 2000;
  /** cron for day-end expiry (UTC) */
  private String dayEndCron = "0 0 10 * * *"; // 15:30 IST ~ 10:00 UTC
  public long getIocTimeoutMs(){ return iocTimeoutMs; }
  public void setIocTimeoutMs(long v){ this.iocTimeoutMs=v; }
  public String getDayEndCron(){ return dayEndCron; }
  public void setDayEndCron(String c){ this.dayEndCron=c; }
}

package com.example.portfolio.housekeeping;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ProcessedKeysTtl {
  private final JdbcTemplate jdbc;
  public ProcessedKeysTtl(JdbcTemplate jdbc, MeterRegistry reg){
    this.jdbc=jdbc;
    Gauge.builder("portfolio_processed_keys_count", this, self -> self.count()).register(reg);
  }
  @Scheduled(cron = "0 */10 * * * *")
  public void clean(){
    jdbc.update("DELETE FROM processed_keys WHERE processed_at < now() - interval '7 days'");
  }
  private double count(){
    Integer c = jdbc.queryForObject("SELECT COUNT(1) FROM processed_keys", Integer.class);
    return c==null?0:c;
  }
}

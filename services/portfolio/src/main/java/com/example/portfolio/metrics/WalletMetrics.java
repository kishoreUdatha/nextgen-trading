package com.example.portfolio.metrics;

import io.micrometer.core.instrument.Gauge;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletMetrics {
  public WalletMetrics(JdbcTemplate jdbc, io.micrometer.core.instrument.MeterRegistry reg){
    Gauge.builder("trading_wallet_balance_min", () -> {
      Double d = jdbc.queryForObject("SELECT MIN(balance) FROM wallet", Double.class);
      return d == null ? 0.0 : d;
    }).register(reg);
  }
}

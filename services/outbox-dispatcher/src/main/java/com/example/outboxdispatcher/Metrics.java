package com.example.outboxdispatcher;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Metrics {
  public static Counter FAILED, SENT;
  public Metrics(MeterRegistry reg){
    FAILED = Counter.builder("trading_outbox_failed_total").register(reg);
    SENT = Counter.builder("trading_outbox_sent_total").register(reg);
  }
}

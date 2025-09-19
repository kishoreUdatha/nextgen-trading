package com.example.poisonreplayer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ReplayerMetrics {
  public static Counter REQUEUED;
  public ReplayerMetrics(MeterRegistry reg){
    REQUEUED = Counter.builder("trading_poison_requeued_total").register(reg);
  }
}

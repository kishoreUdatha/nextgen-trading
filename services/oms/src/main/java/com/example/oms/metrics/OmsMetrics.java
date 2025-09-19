package com.example.oms.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OmsMetrics {
  public final Counter ordersPlaced;
  public final Counter ordersRejected;
  public final Counter ordersFilled;
  public final Counter ordersCancelled;

  public OmsMetrics(MeterRegistry reg){
    ordersPlaced = reg.counter("oms_orders_placed_total");
    ordersRejected = reg.counter("oms_orders_rejected_total");
    ordersFilled = reg.counter("oms_orders_filled_total");
    ordersCancelled = reg.counter("oms_orders_cancelled_total");
  }
}

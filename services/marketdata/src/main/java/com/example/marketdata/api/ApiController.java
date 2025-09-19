package com.example.marketdata.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

record Ltp(String symbol, double ltp){}
record PriceBand(String symbol, double lower, double upper){}

@RestController
@RequestMapping(path="/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {

  @GetMapping("/ltp/{symbol}")
  public Mono<Ltp> ltp(@PathVariable String symbol){
    return Mono.just(new Ltp(symbol, 100.0));
  }

  @GetMapping("/price-bands/{symbol}")
  public Mono<PriceBand> bands(@PathVariable String symbol){
    return Mono.just(new PriceBand(symbol, 90.0, 110.0));
  }
}

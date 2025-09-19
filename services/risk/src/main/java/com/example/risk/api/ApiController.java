package com.example.risk.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

record RiskStatus(String orderId, String decision, String reason){}

@RestController
@RequestMapping(path="/api/v1/risk", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {

  @GetMapping("/healthcheck")
  public Mono<RiskStatus> hc(){
    return Mono.just(new RiskStatus("N/A","APPROVED","skeleton"));
  }
}

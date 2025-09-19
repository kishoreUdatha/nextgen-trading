package com.example.exec.adapter.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

record ExecConfig(double fillRatio, int minLatencyMs, int maxLatencyMs){}

@RestController
@RequestMapping(path="/api/v1/exec", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {

  @GetMapping("/config")
  public Mono<ExecConfig> cfg(){
    return Mono.just(new ExecConfig(0.7,120,800));
  }
}

package com.example.risk.api;

import com.example.risk.messaging.RiskConsumer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping(path="/api/v1/blocks", produces = MediaType.APPLICATION_JSON_VALUE)
public class BlocksApi {
  @GetMapping("/recent")
  public Mono<List<String>> recent(){
    return Mono.just(RiskConsumer.RECENT_BLOCKS.stream().toList());
  }
}

package com.example.portfolio.api;

import com.example.portfolio.domain.Position;
import com.example.portfolio.domain.PositionRepo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path="/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController {
  private final PositionRepo repo;
  public ApiController(PositionRepo repo){ this.repo=repo; }

  @GetMapping("/positions")
  public Flux<Position> positions(){
    return Flux.fromIterable(repo.findAll());
  }
}

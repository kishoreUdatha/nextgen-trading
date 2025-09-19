package com.example.portfolio.api;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping(path="/api/v1/wallet", produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletApi {
  private final JdbcTemplate jdbc;
  public WalletApi(JdbcTemplate jdbc){ this.jdbc=jdbc; }

  @GetMapping("/{userId}")
  public Mono<Map<String,Object>> get(@PathVariable String userId){
    java.math.BigDecimal bal = jdbc.queryForObject("SELECT COALESCE(balance,0) FROM wallet WHERE user_id = ?",
      (rs, rn) -> rs.getBigDecimal(1));
    return Mono.just(Map.of("userId", userId, "balance", bal));
  }

  @PostMapping("/{userId}/add/{amount}")
  public Mono<Map<String,Object>> add(@PathVariable String userId, @PathVariable double amount){
    jdbc.update("INSERT INTO cash_ledger(user_id, delta, reason) VALUES (?,?,?)", userId, amount, "TOPUP");
    return get(userId);
  }
}

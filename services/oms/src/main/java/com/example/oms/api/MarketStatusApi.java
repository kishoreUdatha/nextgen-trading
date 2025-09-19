package com.example.oms.api;

import com.example.oms.config.MarketSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.*;

record MarketStatus(boolean open, String openIst, String closeIst){}

@RestController
@RequestMapping(path="/api/v1/market", produces = MediaType.APPLICATION_JSON_VALUE)
public class MarketStatusApi {
  private final MarketSession session;
  public MarketStatusApi(MarketSession session){ this.session=session; }

  @GetMapping("/status")
  public Mono<MarketStatus> status(){
    ZoneId ist = ZoneId.of("Asia/Kolkata");
    LocalDate d = LocalDate.now(ist);
    LocalTime now = LocalTime.now(ist);
    boolean holiday = session.getHolidays().contains(d.toString());
    LocalTime open = LocalTime.parse(session.getOpenIst());
    LocalTime close = LocalTime.parse(session.getCloseIst());
    boolean openNow = !holiday && !now.isBefore(open) && !now.isAfter(close);
    return Mono.just(new MarketStatus(openNow, session.getOpenIst(), session.getCloseIst()));
  }
}

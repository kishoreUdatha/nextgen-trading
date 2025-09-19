package com.example.oms.api;

import com.example.oms.holidays.HolidayService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.LocalDate;

@RestController
@RequestMapping(path="/admin/holidays", produces = MediaType.APPLICATION_JSON_VALUE)
public class HolidayAdminApi {
  private final HolidayService service;
  public HolidayAdminApi(HolidayService service){ this.service=service; }

  @PostMapping("/add")
  public Mono<Object> add(@RequestParam String date, @RequestParam(defaultValue="manual") String source, @RequestParam(defaultValue="") String description){
    service.add(LocalDate.parse(date), source, description);
      return Mono.just("{\"status\":\"ok\"}");
  }

  @PostMapping("/sync")
  public Mono<Object> sync(){
    service.syncSession();
    return Mono.just("{\"status\":\"synced\"}");
  }
}

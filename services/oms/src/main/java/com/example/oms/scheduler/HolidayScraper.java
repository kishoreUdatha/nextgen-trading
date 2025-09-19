package com.example.oms.scheduler;

import com.example.oms.holidays.HolidayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Component
public class HolidayScraper {
  private final HolidayService service;
  private final String url;
  private final WebClient http;

  public HolidayScraper(HolidayService service, @Value("${app.market.holidaysUrl:}") String url){
    this.service=service; this.url=url; this.http = WebClient.create();
  }

  @Scheduled(cron = "0 0 3 * * *") // daily 3:00 UTC
  public void fetch(){
    if (url==null || url.isBlank()) return;
    try {
      String csv = http.get().uri(url).retrieve().bodyToMono(String.class).block();
      if (csv==null || csv.isBlank()) return;
      for (String line : csv.split("\r?\n")){
        if (line.trim().isEmpty()) continue;
        String[] parts = line.split(",");
        String ds = parts[0].trim();
        String desc = parts.length>1 ? parts[1].trim() : "";
        if (ds.matches("\\d{4}-\\d{2}-\\d{2}")){
          service.add(LocalDate.parse(ds), "nse", desc);
        }
      }
    } catch (Exception ignored){}
  }
}

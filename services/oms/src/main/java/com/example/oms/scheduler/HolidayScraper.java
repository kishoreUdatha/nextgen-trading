package com.example.oms.scheduler;

import com.example.oms.holidays.HolidayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
public class HolidayScraper {

    private final HolidayService service;
    private final String url;
    private final RestTemplate restTemplate;

    public HolidayScraper(HolidayService service,
                          @Value("${app.market.holidaysUrl:}") String url) {
        this.service = service;
        this.url = url;
        this.restTemplate = new RestTemplate(); // ✅ Use RestTemplate instead of WebClient
    }

    @Scheduled(cron = "0 0 3 * * *") // daily 3:00 UTC
    public void fetch() {
        if (url == null || url.isBlank()) return;
        try {
            String csv = restTemplate.getForObject(url, String.class); // ✅ Blocking call
            if (csv == null || csv.isBlank()) return;

            for (String line : csv.split("\\r?\\n")) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                String ds = parts[0].trim();
                String desc = parts.length > 1 ? parts[1].trim() : "";
                if (ds.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    service.add(LocalDate.parse(ds), "nse", desc);
                }
            }
        } catch (Exception ex) {
            // ✅ Don’t swallow silently — log it for debugging
            System.err.println("HolidayScraper fetch failed: " + ex.getMessage());
        }
    }
}

package com.example.oms.api;

import com.example.oms.holidays.HolidayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping(path = "/admin/holidays", produces = MediaType.APPLICATION_JSON_VALUE)
public class HolidayAdminApi {

    private final HolidayService service;

    public HolidayAdminApi(HolidayService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> add(
            @RequestParam String date,
            @RequestParam(defaultValue = "manual") String source,
            @RequestParam(defaultValue = "") String description) {

        service.add(LocalDate.parse(date), source, description);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> sync() {
        service.syncSession();
        return ResponseEntity.ok(Map.of("status", "synced"));
    }
}

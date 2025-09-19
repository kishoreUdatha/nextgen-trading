package com.example.oms.admin;

import com.example.oms.config.MarketSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="/admin/holidays", produces = MediaType.APPLICATION_JSON_VALUE)
public class HolidayImportController {
  private final MarketSession session;
  public HolidayImportController(MarketSession session){ this.session=session; }

  @PostMapping(value="/import", consumes = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<Object> importCsv(@RequestBody String csv){
    Set<String> dates = new HashSet<>(session.getHolidays());
    try (BufferedReader br = new BufferedReader(new StringReader(csv))) {
      dates.addAll(br.lines()
        .filter(l -> !l.trim().isEmpty())
        .map(l -> l.split(",")[0].trim())
        .filter(s -> s.matches("\\d{4}-\\d{2}-\\d{2}"))
        .collect(Collectors.toSet()));
    } catch (Exception ignored){}
    session.setHolidays(dates.stream().sorted().toList());
    return new ResponseEntity<>(session.getHolidays(), HttpStatus.OK);
  }
}

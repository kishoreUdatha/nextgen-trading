package com.example.oms.holidays;

import com.example.oms.config.MarketSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HolidayService {
  private final HolidayRepo repo;
  private final MarketSession session;
  public HolidayService(HolidayRepo repo,
                        MarketSession session){ this.repo=repo; this.session=session; }

  @Transactional(readOnly = true)
  public void syncSession(){
    List<String> dates = repo.findAll().stream().map(r -> r.getDate().toString()).sorted().collect(Collectors.toList());
    session.setHolidays(dates);
  }

  @Transactional
  public void add(LocalDate d, String source, String desc){
    if (repo.findByDate(d).isEmpty()){
      HolidayRow r = new HolidayRow();
      r.setDate(d); r.setSource(source); r.setDescription(desc);
      repo.save(r);
      syncSession();
    }
  }
}

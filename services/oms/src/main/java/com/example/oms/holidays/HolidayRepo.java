package com.example.oms.holidays;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HolidayRepo extends JpaRepository<HolidayRow, Integer> {
  Optional<HolidayRow> findByDate(LocalDate date);
}

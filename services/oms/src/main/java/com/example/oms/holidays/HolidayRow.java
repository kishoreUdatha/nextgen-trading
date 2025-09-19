package com.example.oms.holidays;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name="holidays")
@Data
public class HolidayRow {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(unique=true, nullable=false)
  private LocalDate date;

  @Column(nullable=false)
  private String source;

  private String description;

  @Column(nullable=false)
  private Instant createdAt = Instant.now();
}

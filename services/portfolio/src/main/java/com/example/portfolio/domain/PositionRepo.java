package com.example.portfolio.domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PositionRepo extends JpaRepository<Position, Long> {
  Optional<Position> findByUserIdAndSymbol(String userId, String symbol);
}

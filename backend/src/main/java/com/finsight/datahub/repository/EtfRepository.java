package com.finsight.datahub.repository;

import com.finsight.datahub.entity.Etf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EtfRepository extends JpaRepository<Etf, Long> {
    Optional<Etf> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);
}

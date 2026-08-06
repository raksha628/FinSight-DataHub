package com.finsight.datahub.repository;

import com.finsight.datahub.entity.Forex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ForexRepository extends JpaRepository<Forex, Long> {
    Optional<Forex> findByBaseCurrencyAndQuoteCurrencyAndTradeDate(String baseCurrency, String quoteCurrency, LocalDate tradeDate);
}

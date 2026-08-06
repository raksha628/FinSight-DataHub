package com.finsight.datahub.repository;

import com.finsight.datahub.entity.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    Optional<Crypto> findBySymbolAndTradeDate(String symbol, LocalDate tradeDate);
}

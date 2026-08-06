package com.finsight.datahub.repository;

import com.finsight.datahub.entity.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {
    Optional<MutualFund> findBySymbolAndNavDate(String symbol, LocalDate navDate);
}

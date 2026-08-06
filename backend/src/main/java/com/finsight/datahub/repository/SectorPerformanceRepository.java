package com.finsight.datahub.repository;

import com.finsight.datahub.entity.SectorPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SectorPerformanceRepository extends JpaRepository<SectorPerformance, Long> {
    Optional<SectorPerformance> findBySectorNameAndPerformanceDate(String sectorName, LocalDate performanceDate);
}

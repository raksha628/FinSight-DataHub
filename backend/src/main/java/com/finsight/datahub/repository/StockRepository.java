package com.finsight.datahub.repository;

import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByCompanyAndTradeDate(Company company, LocalDate tradeDate);

    @Query("SELECT s FROM Stock s WHERE s.company.symbol = :symbol AND s.tradeDate < :date ORDER BY s.tradeDate DESC LIMIT 1")
    Optional<Stock> findPreviousTradeDateStock(@Param("symbol") String symbol, @Param("date") LocalDate date);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:tradeDate as date) IS NULL OR s.tradeDate = :tradeDate) AND (cast(:sector as string) IS NULL OR c.sector = :sector) AND (cast(:symbol as string) IS NULL OR c.symbol = :symbol) AND s.dailyReturn IS NOT NULL ORDER BY s.dailyReturn DESC")
    Page<Stock> findTopGainers(@Param("tradeDate") LocalDate tradeDate, @Param("sector") String sector, @Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:tradeDate as date) IS NULL OR s.tradeDate = :tradeDate) AND (cast(:sector as string) IS NULL OR c.sector = :sector) AND (cast(:symbol as string) IS NULL OR c.symbol = :symbol) AND s.dailyReturn IS NOT NULL ORDER BY s.dailyReturn ASC")
    Page<Stock> findTopLosers(@Param("tradeDate") LocalDate tradeDate, @Param("sector") String sector, @Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:tradeDate as date) IS NULL OR s.tradeDate = :tradeDate) AND (cast(:sector as string) IS NULL OR c.sector = :sector) AND (cast(:symbol as string) IS NULL OR c.symbol = :symbol) ORDER BY s.volume DESC")
    Page<Stock> findHighestVolume(@Param("tradeDate") LocalDate tradeDate, @Param("sector") String sector, @Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT new com.finsight.datahub.dto.response.SectorAvgPriceDto(c.sector, AVG(s.closePrice), SUM(s.volume), COUNT(DISTINCT c.id)) FROM Stock s JOIN s.company c WHERE (cast(:tradeDate as date) IS NULL OR s.tradeDate = :tradeDate) GROUP BY c.sector ORDER BY AVG(s.closePrice) DESC")
    List<SectorAvgPriceDto> findSectorAveragePrices(@Param("tradeDate") LocalDate tradeDate);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:startDate as date) IS NULL OR s.tradeDate >= :startDate) AND (cast(:endDate as date) IS NULL OR s.tradeDate <= :endDate) AND (cast(:sector as string) IS NULL OR c.sector = :sector) AND (cast(:symbol as string) IS NULL OR c.symbol = :symbol) ORDER BY s.closePrice DESC")
    Page<Stock> findHighestClosingPrice(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("sector") String sector, @Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:startDate as date) IS NULL OR s.tradeDate >= :startDate) AND (cast(:endDate as date) IS NULL OR s.tradeDate <= :endDate) AND (cast(:sector as string) IS NULL OR c.sector = :sector) AND (cast(:symbol as string) IS NULL OR c.symbol = :symbol) ORDER BY s.closePrice ASC")
    Page<Stock> findLowestClosingPrice(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("sector") String sector, @Param("symbol") String symbol, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE (cast(:symbol as string) IS NULL OR c.symbol = :symbol) AND (cast(:startDate as date) IS NULL OR s.tradeDate >= :startDate) AND (cast(:endDate as date) IS NULL OR s.tradeDate <= :endDate) ORDER BY s.tradeDate ASC")
    List<Stock> findStockHistoryForMovingAverage(@Param("symbol") String symbol, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Stock s JOIN FETCH s.company c WHERE c.symbol = :symbol AND (cast(:startDate as date) IS NULL OR s.tradeDate >= :startDate) AND (cast(:endDate as date) IS NULL OR s.tradeDate <= :endDate) ORDER BY s.tradeDate ASC")
    List<Stock> findStockHistoryBySymbolAndDateRange(@Param("symbol") String symbol, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(s.closePrice) FROM Stock s")
    BigDecimal findAverageMarketPrice();

    @Query("SELECT s.tradeDate FROM Stock s ORDER BY s.tradeDate DESC LIMIT 1")
    Optional<LocalDate> findLatestTradeDate();
}

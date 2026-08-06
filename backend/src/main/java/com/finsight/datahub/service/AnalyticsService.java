package com.finsight.datahub.service;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.PeriodReturnDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    Page<StockPerformanceDto> getTopGainers(LocalDate date, String sector, String symbol, Pageable pageable);

    Page<StockPerformanceDto> getTopLosers(LocalDate date, String sector, String symbol, Pageable pageable);

    Page<StockPerformanceDto> getHighestVolume(LocalDate date, String sector, String symbol, Pageable pageable);

    List<SectorAvgPriceDto> getAveragePriceBySector(LocalDate date);

    Page<PeriodReturnDto> getDailyReturns(LocalDate date, String sector, String symbol, Pageable pageable);

    Page<PeriodReturnDto> getWeeklyReturns(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable);

    Page<PeriodReturnDto> getMonthlyReturns(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable);

    List<MovingAverageDto> getMovingAverages(String symbol, LocalDate startDate, LocalDate endDate);

    Page<StockPerformanceDto> getHighestClosingPrice(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable);

    Page<StockPerformanceDto> getLowestClosingPrice(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable);

    Page<StockPerformanceDto> getMostActiveStocks(LocalDate date, String sector, String symbol, Pageable pageable);
}

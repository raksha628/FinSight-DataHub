package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.*;
import com.finsight.datahub.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Financial Market Analytics and Calculation Engines")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/top-gainers")
    @Operation(summary = "Get top gaining stocks", description = "Returns stocks ordered by highest percentage daily return.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getTopGainers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getTopGainers(date, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Top gainers retrieved successfully", result));
    }

    @GetMapping("/top-losers")
    @Operation(summary = "Get top losing stocks", description = "Returns stocks ordered by lowest percentage daily return.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getTopLosers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getTopLosers(date, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Top losers retrieved successfully", result));
    }

    @GetMapping("/volume")
    @Operation(summary = "Get highest volume stocks", description = "Returns stocks sorted by total shares traded volume.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getHighestVolume(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getHighestVolume(date, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Volume analytics retrieved successfully", result));
    }

    @GetMapping("/sector-avg-price")
    @Operation(summary = "Get average price by sector", description = "Returns average closing stock prices grouped by market sector.")
    public ResponseEntity<ApiResponse<List<SectorAvgPriceDto>>> getAveragePriceBySector(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<SectorAvgPriceDto> result = analyticsService.getAveragePriceBySector(date);
        return ResponseEntity.ok(ApiResponse.success("Sector average prices retrieved successfully", result));
    }

    @GetMapping("/returns/daily")
    @Operation(summary = "Get daily percentage returns", description = "Calculates daily return metrics per stock.")
    public ResponseEntity<ApiResponse<Page<PeriodReturnDto>>> getDailyReturns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PeriodReturnDto> result = analyticsService.getDailyReturns(date, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Daily returns retrieved successfully", result));
    }

    @GetMapping("/returns/weekly")
    @Operation(summary = "Get weekly percentage returns", description = "Calculates rolling 7-day or custom weekly returns.")
    public ResponseEntity<ApiResponse<Page<PeriodReturnDto>>> getWeeklyReturns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PeriodReturnDto> result = analyticsService.getWeeklyReturns(startDate, endDate, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Weekly returns retrieved successfully", result));
    }

    @GetMapping("/returns/monthly")
    @Operation(summary = "Get monthly percentage returns", description = "Calculates 30-day or monthly return metrics.")
    public ResponseEntity<ApiResponse<Page<PeriodReturnDto>>> getMonthlyReturns(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PeriodReturnDto> result = analyticsService.getMonthlyReturns(startDate, endDate, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Monthly returns retrieved successfully", result));
    }

    @GetMapping("/moving-average")
    @Operation(summary = "Get technical SMA moving averages", description = "Calculates 20-day (SMA20) and 50-day (SMA50) simple moving averages.")
    public ResponseEntity<ApiResponse<List<MovingAverageDto>>> getMovingAverage(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<MovingAverageDto> result = analyticsService.getMovingAverages(symbol, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Moving averages retrieved successfully", result));
    }

    @GetMapping("/highest-close")
    @Operation(summary = "Get highest closing prices", description = "Returns peak closing stock prices recorded in date range.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getHighestClose(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getHighestClosingPrice(startDate, endDate, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Highest closing prices retrieved successfully", result));
    }

    @GetMapping("/lowest-close")
    @Operation(summary = "Get lowest closing prices", description = "Returns minimum closing stock prices recorded in date range.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getLowestClose(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getLowestClosingPrice(startDate, endDate, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lowest closing prices retrieved successfully", result));
    }

    @GetMapping("/most-active")
    @Operation(summary = "Get most active stocks", description = "Returns most actively traded equities ranked by volume.")
    public ResponseEntity<ApiResponse<Page<StockPerformanceDto>>> getMostActive(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StockPerformanceDto> result = analyticsService.getMostActiveStocks(date, sector, symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success("Most active stocks retrieved successfully", result));
    }

    @GetMapping("/sector-performance")
    @Operation(summary = "Get sector performance summary", description = "Returns aggregated sector performance summary statistics.")
    public ResponseEntity<ApiResponse<List<SectorAvgPriceDto>>> getSectorPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<SectorAvgPriceDto> result = analyticsService.getAveragePriceBySector(date);
        return ResponseEntity.ok(ApiResponse.success("Sector performance summary retrieved successfully", result));
    }
}

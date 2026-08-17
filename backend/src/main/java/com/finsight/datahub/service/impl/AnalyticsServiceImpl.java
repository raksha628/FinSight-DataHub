package com.finsight.datahub.service.impl;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.PeriodReturnDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.service.AnalyticsService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final StockRepository stockRepository;

    public AnalyticsServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public Page<StockPerformanceDto> getTopGainers(LocalDate date, String sector, String symbol, Pageable pageable) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        Page<Stock> stocks = stockRepository.findTopGainers(queryDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    @Override
    public Page<StockPerformanceDto> getTopLosers(LocalDate date, String sector, String symbol, Pageable pageable) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        Page<Stock> stocks = stockRepository.findTopLosers(queryDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    @Override
    public Page<StockPerformanceDto> getHighestVolume(LocalDate date, String sector, String symbol, Pageable pageable) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        Page<Stock> stocks = stockRepository.findHighestVolume(queryDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    @Override
    public List<SectorAvgPriceDto> getAveragePriceBySector(LocalDate date) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        return stockRepository.findSectorAveragePrices(queryDate);
    }

    @Override
    public Page<PeriodReturnDto> getDailyReturns(LocalDate date, String sector, String symbol, Pageable pageable) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        Page<Stock> stocks = stockRepository.findTopGainers(queryDate, sector, symbol, pageable);

        List<PeriodReturnDto> returns = stocks.getContent().stream().map(s -> {
            BigDecimal open = s.getOpenPrice();
            BigDecimal close = s.getClosePrice();
            BigDecimal change = close.subtract(open);
            BigDecimal pctReturn = s.getDailyReturn() != null ? s.getDailyReturn().multiply(BigDecimal.valueOf(100)) :
                    (open.compareTo(BigDecimal.ZERO) > 0 ? change.divide(open, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

            return new PeriodReturnDto(
                    s.getCompany().getSymbol(),
                    s.getCompany().getName(),
                    s.getCompany().getSector(),
                    s.getTradeDate(),
                    s.getTradeDate(),
                    open,
                    close,
                    change,
                    pctReturn
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(returns, pageable, stocks.getTotalElements());
    }

    @Override
    public Page<PeriodReturnDto> getWeeklyReturns(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusWeeks(1);
        return calculatePeriodReturns(start, end, sector, symbol, pageable);
    }

    @Override
    public Page<PeriodReturnDto> getMonthlyReturns(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusMonths(1);
        return calculatePeriodReturns(start, end, sector, symbol, pageable);
    }

    @Override
    public Page<MovingAverageDto> getMovingAverages(String symbol, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (symbol == null || symbol.isBlank()) {
            symbol = "AAPL"; // Default fallback symbol
        }
        List<Stock> history = stockRepository.findStockHistoryForMovingAverage(symbol.toUpperCase(), startDate, endDate);
        List<MovingAverageDto> allMAs = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            Stock current = history.get(i);
            BigDecimal sma20 = calculateSma(history, i, 20);
            BigDecimal sma50 = calculateSma(history, i, 50);

            allMAs.add(new MovingAverageDto(
                    current.getCompany().getSymbol(),
                    current.getCompany().getName(),
                    current.getTradeDate(),
                    current.getClosePrice(),
                    sma20,
                    sma50
            ));
        }

        int start = (int) pageable.getOffset();
        if (start >= allMAs.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, allMAs.size());
        }
        int end = Math.min((start + pageable.getPageSize()), allMAs.size());
        List<MovingAverageDto> pageContent = allMAs.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allMAs.size());
    }

    @Override
    public Page<StockPerformanceDto> getHighestClosingPrice(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable) {
        Page<Stock> stocks = stockRepository.findHighestClosingPrice(startDate, endDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    @Override
    public Page<StockPerformanceDto> getLowestClosingPrice(LocalDate startDate, LocalDate endDate, String sector, String symbol, Pageable pageable) {
        Page<Stock> stocks = stockRepository.findLowestClosingPrice(startDate, endDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    @Override
    public Page<StockPerformanceDto> getMostActiveStocks(LocalDate date, String sector, String symbol, Pageable pageable) {
        LocalDate queryDate = date != null ? date : stockRepository.findLatestTradeDate().orElse(null);
        Page<Stock> stocks = stockRepository.findHighestVolume(queryDate, sector, symbol, pageable);
        return stocks.map(this::mapToStockPerformanceDto);
    }

    private Page<PeriodReturnDto> calculatePeriodReturns(LocalDate start, LocalDate end, String sector, String symbol, Pageable pageable) {
        Page<Stock> stocks = stockRepository.findHighestVolume(end, sector, symbol, pageable);

        List<PeriodReturnDto> returns = stocks.getContent().stream().map(s -> {
            String sym = s.getCompany().getSymbol();
            List<Stock> hist = stockRepository.findStockHistoryBySymbolAndDateRange(sym, start, end);

            BigDecimal startPrice = !hist.isEmpty() ? hist.get(0).getOpenPrice() : s.getOpenPrice();
            BigDecimal endPrice = !hist.isEmpty() ? hist.get(hist.size() - 1).getClosePrice() : s.getClosePrice();
            BigDecimal change = endPrice.subtract(startPrice);
            BigDecimal pctReturn = startPrice.compareTo(BigDecimal.ZERO) > 0 ?
                    change.divide(startPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

            return new PeriodReturnDto(
                    s.getCompany().getSymbol(),
                    s.getCompany().getName(),
                    s.getCompany().getSector(),
                    start,
                    end,
                    startPrice,
                    endPrice,
                    change,
                    pctReturn
            );
        }).collect(Collectors.toList());

        return new PageImpl<>(returns, pageable, stocks.getTotalElements());
    }

    private BigDecimal calculateSma(List<Stock> history, int currentIndex, int period) {
        int start = Math.max(0, currentIndex - period + 1);
        int count = currentIndex - start + 1;
        BigDecimal sum = BigDecimal.ZERO;

        for (int k = start; k <= currentIndex; k++) {
            sum = sum.add(history.get(k).getClosePrice());
        }

        return sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }

    private StockPerformanceDto mapToStockPerformanceDto(Stock stock) {
        BigDecimal change = stock.getClosePrice().subtract(stock.getOpenPrice());
        return new StockPerformanceDto(
                stock.getCompany().getSymbol(),
                stock.getCompany().getName(),
                stock.getCompany().getSector(),
                stock.getTradeDate(),
                stock.getOpenPrice(),
                stock.getHighPrice(),
                stock.getLowPrice(),
                stock.getClosePrice(),
                stock.getVolume(),
                stock.getDailyReturn(),
                change
        );
    }
}

package com.finsight.datahub.service;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    private AnalyticsServiceImpl analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(stockRepository);
    }

    @Test
    void testGetTopGainers() {
        LocalDate today = LocalDate.of(2026, 1, 2);
        Company comp = new Company("AAPL", "Apple Inc", "Technology", "Consumer Electronics", "USA", "NASDAQ");
        Stock stock = new Stock();
        stock.setCompany(comp);
        stock.setTradeDate(today);
        stock.setOpenPrice(new BigDecimal("180.00"));
        stock.setHighPrice(new BigDecimal("185.00"));
        stock.setLowPrice(new BigDecimal("179.00"));
        stock.setClosePrice(new BigDecimal("184.00"));
        stock.setVolume(50000000L);
        stock.setDailyReturn(new BigDecimal("0.022222"));

        Pageable pageable = PageRequest.of(0, 10);
        when(stockRepository.findLatestTradeDate()).thenReturn(Optional.of(today));
        when(stockRepository.findTopGainers(eq(today), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(stock), pageable, 1));

        Page<StockPerformanceDto> result = analyticsService.getTopGainers(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("AAPL", result.getContent().get(0).getSymbol());
        assertEquals(new BigDecimal("184.00"), result.getContent().get(0).getClosePrice());
    }

    @Test
    void testGetMovingAverages() {
        Company comp = new Company("MSFT", "Microsoft Corp", "Technology", "Software", "USA", "NASDAQ");
        Stock s1 = createStock(comp, LocalDate.of(2026, 1, 1), "100.00");
        Stock s2 = createStock(comp, LocalDate.of(2026, 1, 2), "110.00");
        Stock s3 = createStock(comp, LocalDate.of(2026, 1, 3), "120.00");

        when(stockRepository.findStockHistoryForMovingAverage(eq("MSFT"), any(), any()))
                .thenReturn(List.of(s1, s2, s3));

        List<MovingAverageDto> result = analyticsService.getMovingAverages("MSFT", null, null);

        assertEquals(3, result.size());
        assertEquals(new BigDecimal("100.0000"), result.get(0).getSma20());
        assertEquals(new BigDecimal("105.0000"), result.get(1).getSma20()); // (100+110)/2
        assertEquals(new BigDecimal("110.0000"), result.get(2).getSma20()); // (100+110+120)/3
    }

    private Stock createStock(Company comp, LocalDate date, String closePrice) {
        Stock stock = new Stock();
        stock.setCompany(comp);
        stock.setTradeDate(date);
        stock.setOpenPrice(new BigDecimal(closePrice));
        stock.setHighPrice(new BigDecimal(closePrice));
        stock.setLowPrice(new BigDecimal(closePrice));
        stock.setClosePrice(new BigDecimal(closePrice));
        stock.setVolume(1000000L);
        return stock;
    }
}

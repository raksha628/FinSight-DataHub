package com.finsight.datahub.service;

import com.finsight.datahub.dto.response.DashboardOverviewDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import com.finsight.datahub.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private UploadService uploadService;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(stockRepository, companyRepository, analyticsService, uploadService);
    }

    @Test
    void testGetDashboardOverview() {
        when(stockRepository.count()).thenReturn(100L);
        when(companyRepository.count()).thenReturn(20L);
        when(stockRepository.findAverageMarketPrice()).thenReturn(new BigDecimal("150.25"));

        StockPerformanceDto topGainer = new StockPerformanceDto();
        topGainer.setSymbol("AAPL");
        when(analyticsService.getTopGainers(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(topGainer)));

        when(analyticsService.getTopLosers(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(analyticsService.getHighestVolume(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(analyticsService.getAveragePriceBySector(any())).thenReturn(Collections.emptyList());
        when(uploadService.getUploadHistory()).thenReturn(Collections.emptyList());

        DashboardOverviewDto overview = dashboardService.getDashboardOverview();

        assertNotNull(overview);
        assertEquals(100L, overview.getTotalStocks());
        assertEquals(20L, overview.getTotalCompanies());
        assertEquals(new BigDecimal("150.25"), overview.getAvgMarketPrice());
        assertEquals("AAPL", overview.getTopGainer().getSymbol());
    }
}

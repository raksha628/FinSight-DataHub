package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.*;
import com.finsight.datahub.service.AnalyticsService;
import com.finsight.datahub.service.DashboardService;
import com.finsight.datahub.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Phase 3 - Dashboard and Reports Controller Integration Tests.
 */
@ExtendWith(MockitoExtension.class)
class DashboardAndReportsIntegrationTest {

    @Mock private DashboardService dashboardService;
    @Mock private UploadService uploadService;
    @Mock private AnalyticsService analyticsService;

    private DashboardController dashboardController;
    private UploadController uploadController;
    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(dashboardService);
        uploadController = new UploadController(uploadService, null);
        analyticsController = new AnalyticsController(analyticsService);
    }

    // 1. Dashboard overview

    @Test
    void dashboardOverview_returnsCorrectAggregatedStructure() {
        StockPerformanceDto gainer = new StockPerformanceDto();
        gainer.setSymbol("NVDA");
        gainer.setClosePrice(new BigDecimal("469.20"));
        gainer.setDailyReturn(new BigDecimal("3.50"));

        StockPerformanceDto loser = new StockPerformanceDto();
        loser.setSymbol("INTC");
        loser.setClosePrice(new BigDecimal("31.55"));
        loser.setDailyReturn(new BigDecimal("-2.10"));

        SectorAvgPriceDto sectorDto = new SectorAvgPriceDto("Technology", new BigDecimal("250.00"), 1000000L, 5L);

        UploadHistoryDto recentUpload = new UploadHistoryDto();
        recentUpload.setId(1L);
        recentUpload.setOriginalFilename("stocks_aug.csv");

        DashboardOverviewDto expected = new DashboardOverviewDto();
        expected.setTotalStocks(120L);
        expected.setTotalCompanies(25L);
        expected.setAvgMarketPrice(new BigDecimal("182.50"));
        expected.setTopGainer(gainer);
        expected.setTopLoser(loser);
        expected.setSectorDistribution(List.of(sectorDto));
        expected.setRecentUploads(List.of(recentUpload));

        when(dashboardService.getDashboardOverview()).thenReturn(expected);

        ResponseEntity<ApiResponse<DashboardOverviewDto>> response = dashboardController.getDashboardOverview();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        DashboardOverviewDto data = response.getBody().getData();
        assertNotNull(data);
        assertEquals(120L, data.getTotalStocks());
        assertEquals(25L, data.getTotalCompanies());
        assertEquals(new BigDecimal("182.50"), data.getAvgMarketPrice());

        assertNotNull(data.getTopGainer());
        assertEquals("NVDA", data.getTopGainer().getSymbol());
        assertEquals(new BigDecimal("469.20"), data.getTopGainer().getClosePrice());

        assertNotNull(data.getTopLoser());
        assertEquals("INTC", data.getTopLoser().getSymbol());

        assertNotNull(data.getSectorDistribution());
        assertEquals(1, data.getSectorDistribution().size());
        assertEquals("Technology", data.getSectorDistribution().get(0).getSector());

        assertNotNull(data.getRecentUploads());
        assertEquals(1, data.getRecentUploads().size());
        assertEquals("stocks_aug.csv", data.getRecentUploads().get(0).getOriginalFilename());
    }

    @Test
    void dashboardOverview_whenNoData_returnsZeroCountsAndNullHighlights() {
        DashboardOverviewDto empty = new DashboardOverviewDto();
        empty.setTotalStocks(0L);
        empty.setTotalCompanies(0L);
        empty.setAvgMarketPrice(BigDecimal.ZERO);
        empty.setSectorDistribution(Collections.emptyList());
        empty.setRecentUploads(Collections.emptyList());

        when(dashboardService.getDashboardOverview()).thenReturn(empty);

        ResponseEntity<ApiResponse<DashboardOverviewDto>> response = dashboardController.getDashboardOverview();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        DashboardOverviewDto data = response.getBody().getData();
        assertEquals(0L, data.getTotalStocks());
        assertEquals(BigDecimal.ZERO, data.getAvgMarketPrice());
        assertNull(data.getTopGainer());
        assertTrue(data.getSectorDistribution().isEmpty());
        assertTrue(data.getRecentUploads().isEmpty());
    }

    // 2. Upload history pagination

    @Test
    void uploadHistory_withPageSize20_returnsOnlyFirst20Records() {
        List<UploadHistoryDto> items = buildUploadHistoryStubs(20);
        Page<UploadHistoryDto> pagedResult = new PageImpl<>(items, PageRequest.of(0, 20), 75);
        when(uploadService.getUploadHistory(any(Pageable.class))).thenReturn(pagedResult);

        ResponseEntity<ApiResponse<Page<UploadHistoryDto>>> response = uploadController.getUploadHistory(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Page<UploadHistoryDto> pageData = response.getBody().getData();
        assertNotNull(pageData);
        assertEquals(20, pageData.getContent().size());
        assertEquals(75L, pageData.getTotalElements());
        assertEquals(0, pageData.getNumber());
        assertEquals(4, pageData.getTotalPages());
    }

    @Test
    void uploadHistory_secondPage_returnsCorrectOffset() {
        List<UploadHistoryDto> items = buildUploadHistoryStubs(10);
        Page<UploadHistoryDto> pagedResult = new PageImpl<>(items, PageRequest.of(1, 10), 35);
        when(uploadService.getUploadHistory(any(Pageable.class))).thenReturn(pagedResult);

        ResponseEntity<ApiResponse<Page<UploadHistoryDto>>> response = uploadController.getUploadHistory(1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<UploadHistoryDto> pageData = response.getBody().getData();
        assertEquals(10, pageData.getContent().size());
        assertEquals(1, pageData.getNumber());
        assertEquals(4, pageData.getTotalPages());
    }

    @Test
    void uploadHistory_whenEmpty_returnsEmptyPage() {
        Page<UploadHistoryDto> empty = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(uploadService.getUploadHistory(any(Pageable.class))).thenReturn(empty);

        ResponseEntity<ApiResponse<Page<UploadHistoryDto>>> response = uploadController.getUploadHistory(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<UploadHistoryDto> pageData = response.getBody().getData();
        assertTrue(pageData.getContent().isEmpty());
        assertEquals(0L, pageData.getTotalElements());
    }

    // 3. Moving averages pagination

    @Test
    void movingAverages_withPageSize10_returnsOnlyFirst10Records() {
        List<MovingAverageDto> items = buildMovingAverageStubs(10);
        Page<MovingAverageDto> pagedResult = new PageImpl<>(items, PageRequest.of(0, 10), 90);
        when(analyticsService.getMovingAverages(any(), any(), any(), any(Pageable.class))).thenReturn(pagedResult);

        ResponseEntity<ApiResponse<Page<MovingAverageDto>>> response =
                analyticsController.getMovingAverage("AAPL", null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        Page<MovingAverageDto> pageData = response.getBody().getData();
        assertEquals(10, pageData.getContent().size());
        assertEquals(90L, pageData.getTotalElements());
        assertEquals(9, pageData.getTotalPages());
    }

    @Test
    void movingAverages_secondPage_returnsCorrectOffset() {
        List<MovingAverageDto> items = buildMovingAverageStubs(10);
        Page<MovingAverageDto> pagedResult = new PageImpl<>(items, PageRequest.of(1, 10), 90);
        when(analyticsService.getMovingAverages(any(), any(), any(), any(Pageable.class))).thenReturn(pagedResult);

        ResponseEntity<ApiResponse<Page<MovingAverageDto>>> response =
                analyticsController.getMovingAverage("AAPL", null, null, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<MovingAverageDto> pageData = response.getBody().getData();
        assertEquals(1, pageData.getNumber());
        assertEquals(10, pageData.getContent().size());
    }

    @Test
    void movingAverages_correctSmaValuesPassedThrough() {
        MovingAverageDto maDto = new MovingAverageDto(
                "MSFT", "Microsoft Corp", LocalDate.of(2026, 8, 13),
                new BigDecimal("415.00"),
                new BigDecimal("410.50"),
                new BigDecimal("400.00")
        );
        Page<MovingAverageDto> pagedResult = new PageImpl<>(List.of(maDto), PageRequest.of(0, 10), 1);
        when(analyticsService.getMovingAverages(eq("MSFT"), any(), any(), any(Pageable.class))).thenReturn(pagedResult);

        ResponseEntity<ApiResponse<Page<MovingAverageDto>>> response =
                analyticsController.getMovingAverage("MSFT", null, null, 0, 10);

        Page<MovingAverageDto> pageData = response.getBody().getData();
        MovingAverageDto result = pageData.getContent().get(0);

        assertEquals("MSFT", result.getSymbol());
        assertEquals(new BigDecimal("415.00"), result.getClosePrice());
        assertEquals(new BigDecimal("410.50"), result.getSma20());
        assertEquals(new BigDecimal("400.00"), result.getSma50());
    }

    // Helpers

    private List<UploadHistoryDto> buildUploadHistoryStubs(int count) {
        List<UploadHistoryDto> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UploadHistoryDto dto = new UploadHistoryDto();
            dto.setId((long) (i + 1));
            dto.setOriginalFilename("batch_" + (i + 1) + ".csv");
            result.add(dto);
        }
        return result;
    }

    private List<MovingAverageDto> buildMovingAverageStubs(int count) {
        List<MovingAverageDto> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(new MovingAverageDto(
                    "AAPL", "Apple Inc.",
                    LocalDate.of(2026, 1, 1).plusDays(i),
                    new BigDecimal("180.00").add(BigDecimal.valueOf(i)),
                    new BigDecimal("178.00"),
                    new BigDecimal("175.00")
            ));
        }
        return result;
    }
}
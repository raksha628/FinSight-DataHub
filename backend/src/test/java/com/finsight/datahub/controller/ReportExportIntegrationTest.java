package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.service.AnalyticsService;
import com.finsight.datahub.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportExportIntegrationTest {

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private UploadService uploadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReportExportController controller = new ReportExportController(analyticsService, uploadService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testExportSectorPerformanceCsv() throws Exception {
        SectorAvgPriceDto row = new SectorAvgPriceDto("Technology", new BigDecimal("150.00"), 1000L, 2L);
        when(analyticsService.getAveragePriceBySector(any())).thenReturn(List.of(row));

        mockMvc.perform(get("/api/reports/export/sector-performance"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sector_performance_report.csv\""))
                .andExpect(content().string("sector,avg_close_price,total_volume,company_count\nTechnology,150.00,1000,2\n"));
    }

    @Test
    void testExportGainersLosersCsv() throws Exception {
        StockPerformanceDto stock = new StockPerformanceDto(
                "AAPL", "Apple Inc.", "Technology", LocalDate.of(2026, 8, 15),
                new BigDecimal("180.0"), new BigDecimal("185.0"), new BigDecimal("179.0"), new BigDecimal("184.0"),
                50000L, new BigDecimal("0.022"), new BigDecimal("4.0")
        );

        when(analyticsService.getTopGainers(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stock)));
        when(analyticsService.getTopLosers(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/reports/export/gainers-losers"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gainers_losers_report.csv\""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GAINER,AAPL,Apple Inc.,Technology,2026-08-15,180.0,185.0,179.0,184.0,50000,0.022\n")));
    }

    @Test
    void testExportMovingAveragesCsv() throws Exception {
        MovingAverageDto ma = new MovingAverageDto("AAPL", "Apple Inc.", LocalDate.of(2026, 8, 15), new BigDecimal("184.0"), new BigDecimal("182.0"), new BigDecimal("180.0"));
        when(analyticsService.getMovingAverages(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ma)));

        mockMvc.perform(get("/api/reports/export/moving-averages"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"moving_averages_report.csv\""))
                .andExpect(content().string("symbol,company_name,trade_date,close_price,sma_20,sma_50\nAAPL,Apple Inc.,2026-08-15,184.0,182.0,180.0\n"));
    }

    @Test
    void testExportEtlAuditCsv() throws Exception {
        UploadHistoryDto dto = new UploadHistoryDto();
        dto.setId(1L);
        dto.setOriginalFilename("test.csv");
        dto.setAssetType(com.finsight.datahub.entity.AssetType.STOCK);
        dto.setStatus(com.finsight.datahub.entity.UploadStatus.SUCCESS);
        dto.setTotalRows(10);
        dto.setAcceptedRows(10);
        dto.setRejectedRows(0);
        dto.setFileSizeBytes(100L);
        dto.setUploadedAt(java.time.LocalDateTime.of(2026, 8, 15, 10, 0));
        dto.setProcessingMs(120L);

        when(uploadService.getUploadHistory(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/reports/export/etl-audit"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"etl_audit_log.csv\""))
                .andExpect(content().string("id,original_filename,asset_type,status,total_rows,accepted_rows,rejected_rows,file_size_bytes,uploaded_at,processing_ms\n1,test.csv,STOCK,SUCCESS,10,10,0,100,2026-08-15T10:00,120\n"));
    }
}

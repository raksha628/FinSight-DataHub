package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    private AnalyticsController analyticsController;

    @BeforeEach
    void setUp() {
        analyticsController = new AnalyticsController(analyticsService);
    }

    @Test
    void testGetTopGainersEndpoint() {
        StockPerformanceDto stock = new StockPerformanceDto();
        stock.setSymbol("NVDA");
        stock.setClosePrice(new BigDecimal("469.20"));

        when(analyticsService.getTopGainers(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stock)));

        ResponseEntity<?> response = analyticsController.getTopGainers(null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

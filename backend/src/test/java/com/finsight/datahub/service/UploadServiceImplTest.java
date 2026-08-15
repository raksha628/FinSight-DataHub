package com.finsight.datahub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.entity.UploadStatus;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.exception.BadRequestException;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.etl.EtlStrategyRegistry;
import com.finsight.datahub.etl.strategy.EtlStrategy;
import com.finsight.datahub.repository.UploadHistoryRepository;
import com.finsight.datahub.service.impl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadServiceImplTest {

    @Mock
    private UploadHistoryRepository uploadHistoryRepository;

    @Mock
    private EtlStrategyRegistry etlStrategyRegistry;

    @Mock
    private EtlStrategy mockStrategy;

    private UploadServiceImpl uploadService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        uploadService = new UploadServiceImpl(uploadHistoryRepository, etlStrategyRegistry, objectMapper);
    }

    @Test
    void testUploadCsvSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "stocks.csv", "text/csv",
                "date,symbol,open,high,low,close,volume\n2026-01-02,AAPL,180,185,179,184,50000".getBytes(StandardCharsets.UTF_8)
        );

        UploadHistory mockHistory = new UploadHistory();
        mockHistory.setId(1L);
        mockHistory.setFilename("stocks.csv");
        mockHistory.setStatus(UploadStatus.PROCESSING);

        when(uploadHistoryRepository.save(any())).thenAnswer(inv -> {
            UploadHistory h = inv.getArgument(0);
            if (h.getId() == null) h.setId(1L);
            return h;
        });

        when(etlStrategyRegistry.getStrategy(AssetType.STOCK)).thenReturn(Optional.of(mockStrategy));

        EtlResult mockResult = new EtlResult();
        mockResult.setTotalRows(1);
        mockResult.setAcceptedRows(1);
        mockResult.setRejectedRows(0);
        when(mockStrategy.process(any(InputStream.class), any(UploadHistory.class))).thenReturn(mockResult);

        User user = new User();
        user.setUsername("analyst");

        UploadResponseDto response = uploadService.uploadCsv(file, AssetType.STOCK, user);

        assertNotNull(response);
        assertEquals(UploadStatus.SUCCESS, response.getStatus());
        assertEquals(1, response.getAcceptedRows());
        assertEquals(0, response.getRejectedRows());
    }

    @Test
    void testUploadEmptyFileThrowsBadRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
        User user = new User();

        assertThrows(BadRequestException.class, () -> uploadService.uploadCsv(emptyFile, AssetType.STOCK, user));
    }

    @Test
    void testGetUploadHistory() {
        UploadHistory history = new UploadHistory();
        history.setId(10L);
        history.setFilename("etf.csv");
        history.setOriginalFilename("etf.csv");
        history.setAssetType(AssetType.ETF);
        history.setStatus(UploadStatus.SUCCESS);

        PageRequest pageable = PageRequest.of(0, 10);
        when(uploadHistoryRepository.findAllByOrderByUploadedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(history), pageable, 1));

        Page<UploadHistoryDto> page = uploadService.getUploadHistory(pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(10L, page.getContent().get(0).getId());
        assertEquals(AssetType.ETF, page.getContent().get(0).getAssetType());
    }
}

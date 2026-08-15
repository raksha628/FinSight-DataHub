package com.finsight.datahub.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.entity.UploadStatus;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.exception.BadRequestException;
import com.finsight.datahub.exception.ResourceNotFoundException;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.etl.EtlStrategyRegistry;
import com.finsight.datahub.etl.strategy.EtlStrategy;
import com.finsight.datahub.repository.UploadHistoryRepository;
import com.finsight.datahub.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    private final UploadHistoryRepository uploadHistoryRepository;
    private final EtlStrategyRegistry etlStrategyRegistry;
    private final ObjectMapper objectMapper;

    public UploadServiceImpl(UploadHistoryRepository uploadHistoryRepository,
                             EtlStrategyRegistry etlStrategyRegistry,
                             ObjectMapper objectMapper) {
        this.uploadHistoryRepository = uploadHistoryRepository;
        this.etlStrategyRegistry = etlStrategyRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    @CacheEvict(cacheNames = {"analytics", "sector", "stocks"}, allEntries = true)
    public UploadResponseDto uploadCsv(MultipartFile file, AssetType assetType, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded CSV file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.csv";
        if (!originalFilename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File must be a valid .csv format");
        }

        try (InputStream is = file.getInputStream()) {
            return uploadFileStream(is, originalFilename, file.getSize(), assetType, user);
        } catch (Exception e) {
            log.error("Failed to read uploaded file stream", e);
            throw new BadRequestException("Failed to read file stream: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"analytics", "sector", "stocks"}, allEntries = true)
    public UploadResponseDto uploadFileStream(InputStream inputStream, String originalFilename, long fileSizeBytes, AssetType assetType, User user) {
        if (assetType == null) {
            throw new BadRequestException("AssetType must be specified");
        }

        EtlStrategy strategy = etlStrategyRegistry.getStrategy(assetType)
                .orElseThrow(() -> new BadRequestException("No ETL strategy registered for asset type: " + assetType));

        String savedFilename = UUID.randomUUID() + "_" + originalFilename;

        UploadHistory history = new UploadHistory();
        history.setFilename(savedFilename);
        history.setOriginalFilename(originalFilename);
        history.setAssetType(assetType);
        history.setStatus(UploadStatus.PROCESSING);
        history.setFileSizeBytes(fileSizeBytes);
        history.setUploadedBy(user);
        history = uploadHistoryRepository.save(history);

        long startTime = System.currentTimeMillis();
        EtlResult result;

        try {
            result = strategy.process(inputStream, history);
        } catch (Exception e) {
            log.error("Fatal error during ETL execution for upload ID {}", history.getId(), e);
            throw new BadRequestException("ETL Processing Failed: " + e.getMessage(), e);
        }

        if (result.getRejectedRows() > 0) {
            String errorMsg = result.getValidationReport().stream()
                    .map(r -> "Row " + r.getRowNumber() + ": " + r.getReason())
                    .collect(Collectors.joining("; "));
            throw new BadRequestException("CSV validation failed: " + errorMsg);
        }

        long duration = System.currentTimeMillis() - startTime;
        history.setTotalRows(result.getTotalRows());
        history.setAcceptedRows(result.getAcceptedRows());
        history.setRejectedRows(0);
        history.setProcessedAt(LocalDateTime.now());
        history.setProcessingMs(duration);
        history.setStatus(UploadStatus.SUCCESS);

        try {
            if (!result.getValidationReport().isEmpty()) {
                history.setValidationReport(objectMapper.writeValueAsString(result.getValidationReport()));
            }
        } catch (JsonProcessingException jpe) {
            log.warn("Could not serialize validation report to JSON", jpe);
        }

        history = uploadHistoryRepository.save(history);

        UploadResponseDto response = new UploadResponseDto();
        response.setUploadId(history.getId());
        response.setFilename(originalFilename);
        response.setAssetType(assetType);
        response.setStatus(history.getStatus());
        response.setTotalRows(result.getTotalRows());
        response.setAcceptedRows(result.getAcceptedRows());
        response.setRejectedRows(0);
        response.setProcessingMs(duration);
        response.setValidationReport(result.getValidationReport());
        response.setMessage(String.format("Ingested %d of %d records successfully in %d ms",
                result.getAcceptedRows(), result.getTotalRows(), duration));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UploadHistoryDto> getUploadHistory(Pageable pageable) {
        Page<UploadHistory> historyPage = uploadHistoryRepository.findAllByOrderByUploadedAtDesc(pageable);
        return historyPage.map(this::mapToHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UploadHistoryDto getUploadDetails(Long id) {
        UploadHistory history = uploadHistoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Upload History not found with ID: " + id));
        return mapToHistoryDto(history);
    }

    private UploadHistoryDto mapToHistoryDto(UploadHistory history) {
        UploadHistoryDto dto = new UploadHistoryDto();
        dto.setId(history.getId());
        dto.setFilename(history.getFilename());
        dto.setOriginalFilename(history.getOriginalFilename());
        dto.setAssetType(history.getAssetType());
        dto.setStatus(history.getStatus());
        dto.setTotalRows(history.getTotalRows());
        dto.setAcceptedRows(history.getAcceptedRows());
        dto.setRejectedRows(history.getRejectedRows());
        dto.setFileSizeBytes(history.getFileSizeBytes());
        dto.setUploadedByUsername(history.getUploadedBy() != null ? history.getUploadedBy().getUsername() : "SYSTEM/BATCH");
        dto.setUploadedAt(history.getUploadedAt());
        dto.setProcessedAt(history.getProcessedAt());
        dto.setProcessingMs(history.getProcessingMs());
        dto.setValidationReport(history.getValidationReport());
        dto.setErrorMessage(history.getErrorMessage());
        return dto;
    }
}

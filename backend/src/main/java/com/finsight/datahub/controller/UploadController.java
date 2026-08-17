package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.ApiResponse;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.repository.UserRepository;
import com.finsight.datahub.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "Upload", description = "ETL CSV Ingestion and Audit History Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final UploadService uploadService;
    private final UserRepository userRepository;

    public UploadController(UploadService uploadService, UserRepository userRepository) {
        this.uploadService = uploadService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(
        summary = "Upload CSV financial market data",
        description = "Ingests financial data (Stocks, ETFs, Mutual Funds) via Strategy-pattern ETL pipeline. Roles: ANALYST, ADMIN."
    )
    public ResponseEntity<ApiResponse<UploadResponseDto>> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("assetType") AssetType assetType,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("CSV Upload request — file: {}, assetType: {}, user: {}",
                file.getOriginalFilename(), assetType, userDetails != null ? userDetails.getUsername() : "anonymous");

        User user = null;
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }

        UploadResponseDto result = uploadService.uploadCsv(file, assetType, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result.getMessage(), result));
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get ETL upload history",
        description = "Retrieves all past CSV ingestion runs with execution metrics and status."
    )
    public ResponseEntity<ApiResponse<Page<UploadHistoryDto>>> getUploadHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UploadHistoryDto> historyPage = uploadService.getUploadHistory(pageable);
        return ResponseEntity.ok(ApiResponse.success("Upload history retrieved successfully", historyPage));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get detailed upload report by ID",
        description = "Retrieves a specific upload history record including row-by-row validation report."
    )
    public ResponseEntity<ApiResponse<UploadHistoryDto>> getUploadDetails(@PathVariable Long id) {
        UploadHistoryDto history = uploadService.getUploadDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Upload details retrieved successfully", history));
    }
}

package com.finsight.datahub.dto.response;

import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadStatus;

import java.time.LocalDateTime;

public class UploadHistoryDto {
    private Long id;
    private String filename;
    private String originalFilename;
    private AssetType assetType;
    private UploadStatus status;
    private Integer totalRows;
    private Integer acceptedRows;
    private Integer rejectedRows;
    private Long fileSizeBytes;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private Long processingMs;
    private String validationReport;
    private String errorMessage;

    public UploadHistoryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public UploadStatus getStatus() { return status; }
    public void setStatus(UploadStatus status) { this.status = status; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getAcceptedRows() { return acceptedRows; }
    public void setAcceptedRows(Integer acceptedRows) { this.acceptedRows = acceptedRows; }

    public Integer getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(Integer rejectedRows) { this.rejectedRows = rejectedRows; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getUploadedByUsername() { return uploadedByUsername; }
    public void setUploadedByUsername(String uploadedByUsername) { this.uploadedByUsername = uploadedByUsername; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Long getProcessingMs() { return processingMs; }
    public void setProcessingMs(Long processingMs) { this.processingMs = processingMs; }

    public String getValidationReport() { return validationReport; }
    public void setValidationReport(String validationReport) { this.validationReport = validationReport; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

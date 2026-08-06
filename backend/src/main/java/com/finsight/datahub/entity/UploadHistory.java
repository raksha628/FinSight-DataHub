package com.finsight.datahub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload_history", indexes = {
    @Index(name = "idx_upload_history_status", columnList = "status"),
    @Index(name = "idx_upload_history_asset_type", columnList = "asset_type"),
    @Index(name = "idx_upload_history_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_upload_history_uploaded_at", columnList = "uploaded_at DESC")
})
public class UploadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 30)
    private AssetType assetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadStatus status = UploadStatus.PROCESSING;

    @Column(name = "total_rows")
    private Integer totalRows = 0;

    @Column(name = "accepted_rows")
    private Integer acceptedRows = 0;

    @Column(name = "rejected_rows")
    private Integer rejectedRows = 0;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_ms")
    private Long processingMs;

    @Column(name = "validation_report", columnDefinition = "TEXT")
    private String validationReport;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public UploadHistory() {}

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

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

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }

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

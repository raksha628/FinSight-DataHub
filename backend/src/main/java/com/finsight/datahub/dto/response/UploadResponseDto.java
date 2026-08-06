package com.finsight.datahub.dto.response;

import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadStatus;

import java.util.List;

public class UploadResponseDto {
    private Long uploadId;
    private String filename;
    private AssetType assetType;
    private UploadStatus status;
    private int totalRows;
    private int acceptedRows;
    private int rejectedRows;
    private long processingMs;
    private List<RowValidationErrorDto> validationReport;
    private String message;

    public UploadResponseDto() {}

    public Long getUploadId() { return uploadId; }
    public void setUploadId(Long uploadId) { this.uploadId = uploadId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public UploadStatus getStatus() { return status; }
    public void setStatus(UploadStatus status) { this.status = status; }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getAcceptedRows() { return acceptedRows; }
    public void setAcceptedRows(int acceptedRows) { this.acceptedRows = acceptedRows; }

    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }

    public long getProcessingMs() { return processingMs; }
    public void setProcessingMs(long processingMs) { this.processingMs = processingMs; }

    public List<RowValidationErrorDto> getValidationReport() { return validationReport; }
    public void setValidationReport(List<RowValidationErrorDto> validationReport) { this.validationReport = validationReport; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

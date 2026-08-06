package com.finsight.datahub.dto.response;

import java.util.Map;

/**
 * Details of a single row validation rejection during CSV ETL processing.
 */
public class RowValidationErrorDto {
    private int rowNumber;
    private String reason;
    private Map<String, String> rowData;

    public RowValidationErrorDto() {}

    public RowValidationErrorDto(int rowNumber, String reason, Map<String, String> rowData) {
        this.rowNumber = rowNumber;
        this.reason = reason;
        this.rowData = rowData;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Map<String, String> getRowData() { return rowData; }
    public void setRowData(Map<String, String> rowData) { this.rowData = rowData; }
}

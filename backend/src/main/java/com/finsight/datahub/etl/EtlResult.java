package com.finsight.datahub.etl;

import com.finsight.datahub.dto.response.RowValidationErrorDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the results of an ETL ingestion job for an asset strategy.
 */
public class EtlResult {
    private int totalRows = 0;
    private int acceptedRows = 0;
    private int rejectedRows = 0;
    private List<RowValidationErrorDto> validationReport = new ArrayList<>();

    public EtlResult() {}

    public void incrementTotal() { this.totalRows++; }
    public void incrementAccepted() { this.acceptedRows++; }
    public void addRejection(RowValidationErrorDto rejection) {
        this.rejectedRows++;
        this.validationReport.add(rejection);
    }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getAcceptedRows() { return acceptedRows; }
    public void setAcceptedRows(int acceptedRows) { this.acceptedRows = acceptedRows; }

    public int getRejectedRows() { return rejectedRows; }
    public void setRejectedRows(int rejectedRows) { this.rejectedRows = rejectedRows; }

    public List<RowValidationErrorDto> getValidationReport() { return validationReport; }
    public void setValidationReport(List<RowValidationErrorDto> validationReport) { this.validationReport = validationReport; }
}

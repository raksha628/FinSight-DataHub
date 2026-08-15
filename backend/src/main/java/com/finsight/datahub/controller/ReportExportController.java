package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.service.AnalyticsService;
import com.finsight.datahub.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Phase 4 — Real CSV Report Export Controller.
 *
 * Every endpoint fetches live data from the database and streams it back as a
 * proper CSV file with correct Content-Disposition and Content-Type headers.
 * No mock data, no placeholder text.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report Export", description = "CSV Report Generation — live data from the data warehouse")
@SecurityRequirement(name = "bearerAuth")
public class ReportExportController {

    private static final int MAX_EXPORT_ROWS = 10_000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AnalyticsService analyticsService;
    private final UploadService uploadService;

    public ReportExportController(AnalyticsService analyticsService, UploadService uploadService) {
        this.analyticsService = analyticsService;
        this.uploadService = uploadService;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  1. Sector Performance Analytics CSV
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/sector-performance")
    @Operation(
        summary = "Export Sector Performance Analytics as CSV",
        description = "Returns a downloadable CSV of average close price and total volume per sector, derived from live stock data."
    )
    public ResponseEntity<byte[]> exportSectorPerformance(
            @RequestParam(required = false) String date) {

        LocalDate tradeDate = date != null ? LocalDate.parse(date, DATE_FMT) : null;
        List<SectorAvgPriceDto> rows = analyticsService.getAveragePriceBySector(tradeDate);

        StringWriter writer = new StringWriter();
        writer.write("sector,avg_close_price,total_volume,company_count\n");
        for (SectorAvgPriceDto row : rows) {
            writer.write(csv(
                    row.getSector(),
                    row.getAvgClosePrice() != null ? row.getAvgClosePrice().toPlainString() : "",
                    row.getTotalVolume() != null ? String.valueOf(row.getTotalVolume()) : "",
                    row.getCompanyCount() != null ? String.valueOf(row.getCompanyCount()) : ""
            ));
        }

        return buildResponse(writer.toString().getBytes(), "sector_performance_report.csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  2. Top Gainers & Losers Daily Audit CSV
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/gainers-losers")
    @Operation(
        summary = "Export Top Gainers & Losers Daily Audit as CSV",
        description = "Returns a downloadable CSV combining the top-gaining and top-losing stocks by daily return percentage."
    )
    public ResponseEntity<byte[]> exportGainersLosers(
            @RequestParam(required = false) String date) {

        LocalDate tradeDate = date != null ? LocalDate.parse(date, DATE_FMT) : null;
        PageRequest bulk = PageRequest.of(0, MAX_EXPORT_ROWS);

        List<StockPerformanceDto> gainers = analyticsService.getTopGainers(tradeDate, null, null, bulk).getContent();
        List<StockPerformanceDto> losers  = analyticsService.getTopLosers(tradeDate, null, null, bulk).getContent();

        StringWriter writer = new StringWriter();
        writer.write("type,symbol,company_name,sector,trade_date,open_price,high_price,low_price,close_price,volume,daily_return\n");
        for (StockPerformanceDto r : gainers) writeStockRow(writer, "GAINER", r);
        for (StockPerformanceDto r : losers)  writeStockRow(writer, "LOSER",  r);

        return buildResponse(writer.toString().getBytes(), "gainers_losers_report.csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  3. Technical Moving Average Summary CSV
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/moving-averages")
    @Operation(
        summary = "Export Technical Moving Average Summary as CSV",
        description = "Returns a downloadable CSV of 20-day and 50-day SMAs across all tracked equity symbols."
    )
    public ResponseEntity<byte[]> exportMovingAverages(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate, DATE_FMT) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate,   DATE_FMT) : null;
        PageRequest bulk = PageRequest.of(0, MAX_EXPORT_ROWS);

        List<MovingAverageDto> rows = analyticsService.getMovingAverages(symbol, start, end, bulk).getContent();

        StringWriter writer = new StringWriter();
        writer.write("symbol,company_name,trade_date,close_price,sma_20,sma_50\n");
        for (MovingAverageDto r : rows) {
            writer.write(csv(
                    r.getSymbol(),
                    r.getCompanyName(),
                    r.getTradeDate() != null ? r.getTradeDate().toString() : "",
                    r.getClosePrice() != null ? r.getClosePrice().toPlainString() : "",
                    r.getSma20()      != null ? r.getSma20().toPlainString()      : "",
                    r.getSma50()      != null ? r.getSma50().toPlainString()      : ""
            ));
        }

        return buildResponse(writer.toString().getBytes(), "moving_averages_report.csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  4. ETL Ingestion Audit Log CSV
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/etl-audit")
    @Operation(
        summary = "Export ETL Ingestion Audit Log as CSV",
        description = "Returns a downloadable CSV of all upload history records including file metadata, row counts, and validation results."
    )
    public ResponseEntity<byte[]> exportEtlAuditLog() {
        List<UploadHistoryDto> rows = uploadService.getUploadHistory(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();

        StringWriter writer = new StringWriter();
        writer.write("id,original_filename,asset_type,status,total_rows,accepted_rows,rejected_rows,file_size_bytes,uploaded_at,processing_ms\n");
        for (UploadHistoryDto r : rows) {
            writer.write(csv(
                    r.getId() != null   ? String.valueOf(r.getId())           : "",
                    r.getOriginalFilename(),
                    r.getAssetType()    != null ? r.getAssetType().toString()  : "",
                    r.getStatus()       != null ? r.getStatus().toString()     : "",
                    r.getTotalRows()    != null ? String.valueOf(r.getTotalRows())    : "",
                    r.getAcceptedRows() != null ? String.valueOf(r.getAcceptedRows()) : "",
                    r.getRejectedRows() != null ? String.valueOf(r.getRejectedRows()) : "",
                    r.getFileSizeBytes()!= null ? String.valueOf(r.getFileSizeBytes()): "",
                    r.getUploadedAt()   != null ? r.getUploadedAt().toString()        : "",
                    r.getProcessingMs() != null ? String.valueOf(r.getProcessingMs()) : ""
            ));
        }

        return buildResponse(writer.toString().getBytes(), "etl_audit_log.csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Build the HTTP response with correct download headers. */
    private ResponseEntity<byte[]> buildResponse(byte[] body, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        return ResponseEntity.ok().headers(headers).body(body);
    }

    /** Escape a CSV field — wrap in quotes if value contains comma, quote, or newline. */
    private String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Build a comma-separated line terminated with \n. */
    private String csv(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsvField(fields[i]));
        }
        sb.append('\n');
        return sb.toString();
    }

    private void writeStockRow(StringWriter writer, String type, StockPerformanceDto r) {
        writer.write(csv(
                type,
                r.getSymbol(),
                r.getCompanyName(),
                r.getSector(),
                r.getTradeDate() != null ? r.getTradeDate().toString() : "",
                r.getOpenPrice()  != null ? r.getOpenPrice().toPlainString()  : "",
                r.getHighPrice()  != null ? r.getHighPrice().toPlainString()  : "",
                r.getLowPrice()   != null ? r.getLowPrice().toPlainString()   : "",
                r.getClosePrice() != null ? r.getClosePrice().toPlainString() : "",
                r.getVolume()     != null ? String.valueOf(r.getVolume())      : "",
                r.getDailyReturn()!= null ? r.getDailyReturn().toPlainString(): ""
        ));
    }
}
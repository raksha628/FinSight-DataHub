package com.finsight.datahub.controller;

import com.finsight.datahub.dto.response.MovingAverageDto;
import com.finsight.datahub.dto.response.SectorAvgPriceDto;
import com.finsight.datahub.dto.response.StockPerformanceDto;
import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.service.AnalyticsService;
import com.finsight.datahub.service.UploadService;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Phase 4 — Real CSV/PDF Report Export Controller.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report Export", description = "CSV & PDF Report Generation — live data from the data warehouse")
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
    //  1. Sector Performance Analytics CSV/PDF
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/sector-performance")
    @Operation(
        summary = "Export Sector Performance Analytics",
        description = "Returns a downloadable CSV or PDF of average close price and total volume per sector."
    )
    public ResponseEntity<byte[]> exportSectorPerformance(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "csv") String format) {

        LocalDate tradeDate = date != null ? LocalDate.parse(date, DATE_FMT) : null;
        List<SectorAvgPriceDto> rows = analyticsService.getAveragePriceBySector(tradeDate);

        if ("pdf".equalsIgnoreCase(format)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document();
                PdfWriter.getInstance(document, baos);
                document.open();

                document.add(new Paragraph("Sector Performance Analytics Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Generated at: " + LocalDate.now().toString()));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.addCell("Sector");
                table.addCell("Avg Close Price");
                table.addCell("Total Volume");
                table.addCell("Company Count");

                for (SectorAvgPriceDto row : rows) {
                    table.addCell(row.getSector());
                    table.addCell(row.getAvgClosePrice() != null ? "$" + row.getAvgClosePrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : "N/A");
                    table.addCell(row.getTotalVolume() != null ? String.valueOf(row.getTotalVolume()) : "N/A");
                    table.addCell(row.getCompanyCount() != null ? String.valueOf(row.getCompanyCount()) : "N/A");
                }

                document.add(table);
                document.close();
                return buildResponse(baos.toByteArray(), "sector_performance_report.pdf", "application/pdf");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }

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

        return buildResponse(writer.toString().getBytes(), "sector_performance_report.csv", "text/csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  2. Top Gainers & Losers Daily Audit CSV/PDF
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/gainers-losers")
    @Operation(
        summary = "Export Top Gainers & Losers Daily Audit",
        description = "Returns a downloadable CSV or PDF combining the top-gaining and top-losing stocks."
    )
    public ResponseEntity<byte[]> exportGainersLosers(
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "csv") String format) {

        LocalDate tradeDate = date != null ? LocalDate.parse(date, DATE_FMT) : null;
        PageRequest bulk = PageRequest.of(0, MAX_EXPORT_ROWS);

        List<StockPerformanceDto> gainers = analyticsService.getTopGainers(tradeDate, null, null, bulk).getContent();
        List<StockPerformanceDto> losers  = analyticsService.getTopLosers(tradeDate, null, null, bulk).getContent();

        if ("pdf".equalsIgnoreCase(format)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document();
                PdfWriter.getInstance(document, baos);
                document.open();

                document.add(new Paragraph("Top Gainers & Losers Daily Audit", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Generated at: " + LocalDate.now().toString()));
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Top Gainers:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(" "));
                PdfPTable gTable = new PdfPTable(6);
                gTable.setWidthPercentage(100);
                gTable.addCell("Symbol");
                gTable.addCell("Company");
                gTable.addCell("Sector");
                gTable.addCell("Close");
                gTable.addCell("Volume");
                gTable.addCell("Daily Return");

                for (StockPerformanceDto r : gainers) {
                    gTable.addCell(r.getSymbol());
                    gTable.addCell(r.getCompanyName());
                    gTable.addCell(r.getSector());
                    gTable.addCell(r.getClosePrice() != null ? "$" + r.getClosePrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : "");
                    gTable.addCell(r.getVolume() != null ? String.valueOf(r.getVolume()) : "");
                    gTable.addCell(r.getDailyReturn() != null ? r.getDailyReturn().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%" : "");
                }
                document.add(gTable);
                document.add(new Paragraph(" "));

                document.add(new Paragraph("Top Losers:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(" "));
                PdfPTable lTable = new PdfPTable(6);
                lTable.setWidthPercentage(100);
                lTable.addCell("Symbol");
                lTable.addCell("Company");
                lTable.addCell("Sector");
                lTable.addCell("Close");
                lTable.addCell("Volume");
                lTable.addCell("Daily Return");

                for (StockPerformanceDto r : losers) {
                    lTable.addCell(r.getSymbol());
                    lTable.addCell(r.getCompanyName());
                    lTable.addCell(r.getSector());
                    lTable.addCell(r.getClosePrice() != null ? "$" + r.getClosePrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : "");
                    lTable.addCell(r.getVolume() != null ? String.valueOf(r.getVolume()) : "");
                    lTable.addCell(r.getDailyReturn() != null ? r.getDailyReturn().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%" : "");
                }
                document.add(lTable);

                document.close();
                return buildResponse(baos.toByteArray(), "gainers_losers_report.pdf", "application/pdf");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }

        StringWriter writer = new StringWriter();
        writer.write("type,symbol,company_name,sector,trade_date,open_price,high_price,low_price,close_price,volume,daily_return\n");
        for (StockPerformanceDto r : gainers) writeStockRow(writer, "GAINER", r);
        for (StockPerformanceDto r : losers)  writeStockRow(writer, "LOSER",  r);

        return buildResponse(writer.toString().getBytes(), "gainers_losers_report.csv", "text/csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  3. Technical Moving Average Summary CSV/PDF
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/moving-averages")
    @Operation(
        summary = "Export Technical Moving Average Summary",
        description = "Returns a downloadable CSV or PDF of 20-day and 50-day SMAs."
    )
    public ResponseEntity<byte[]> exportMovingAverages(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "csv") String format) {

        LocalDate start = startDate != null ? LocalDate.parse(startDate, DATE_FMT) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate,   DATE_FMT) : null;
        PageRequest bulk = PageRequest.of(0, MAX_EXPORT_ROWS);

        List<MovingAverageDto> rows = analyticsService.getMovingAverages(symbol, start, end, bulk).getContent();

        if ("pdf".equalsIgnoreCase(format)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document();
                PdfWriter.getInstance(document, baos);
                document.open();

                document.add(new Paragraph("Technical Moving Average Summary", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Generated at: " + LocalDate.now().toString()));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.addCell("Symbol");
                table.addCell("Company");
                table.addCell("Trade Date");
                table.addCell("Close");
                table.addCell("SMA-20");
                table.addCell("SMA-50");

                for (MovingAverageDto r : rows) {
                    table.addCell(r.getSymbol());
                    table.addCell(r.getCompanyName());
                    table.addCell(r.getTradeDate() != null ? r.getTradeDate().toString() : "");
                    table.addCell(r.getClosePrice() != null ? "$" + r.getClosePrice().setScale(2, RoundingMode.HALF_UP).toPlainString() : "");
                    table.addCell(r.getSma20() != null ? "$" + r.getSma20().setScale(2, RoundingMode.HALF_UP).toPlainString() : "N/A");
                    table.addCell(r.getSma50() != null ? "$" + r.getSma50().setScale(2, RoundingMode.HALF_UP).toPlainString() : "N/A");
                }

                document.add(table);
                document.close();
                return buildResponse(baos.toByteArray(), "moving_averages_report.pdf", "application/pdf");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }

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

        return buildResponse(writer.toString().getBytes(), "moving_averages_report.csv", "text/csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  4. ETL Ingestion Audit Log CSV/PDF
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/export/etl-audit")
    @Operation(
        summary = "Export ETL Ingestion Audit Log",
        description = "Returns a downloadable CSV or PDF of all upload history records."
    )
    public ResponseEntity<byte[]> exportEtlAuditLog(
            @RequestParam(defaultValue = "csv") String format) {
        List<UploadHistoryDto> rows = uploadService.getUploadHistory(PageRequest.of(0, MAX_EXPORT_ROWS)).getContent();

        if ("pdf".equalsIgnoreCase(format)) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Document document = new Document();
                PdfWriter.getInstance(document, baos);
                document.open();

                document.add(new Paragraph("ETL Ingestion Audit Log", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
                document.add(new Paragraph("Generated at: " + LocalDate.now().toString()));
                document.add(new Paragraph(" "));

                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.addCell("ID");
                table.addCell("Filename");
                table.addCell("Asset Type");
                table.addCell("Status");
                table.addCell("Total Rows");
                table.addCell("Accepted");
                table.addCell("Processing (ms)");

                for (UploadHistoryDto r : rows) {
                    table.addCell(r.getId() != null ? String.valueOf(r.getId()) : "");
                    table.addCell(r.getOriginalFilename());
                    table.addCell(r.getAssetType() != null ? r.getAssetType().toString() : "");
                    table.addCell(r.getStatus() != null ? r.getStatus().toString() : "");
                    table.addCell(r.getTotalRows() != null ? String.valueOf(r.getTotalRows()) : "");
                    table.addCell(r.getAcceptedRows() != null ? String.valueOf(r.getAcceptedRows()) : "");
                    table.addCell(r.getProcessingMs() != null ? String.valueOf(r.getProcessingMs()) : "");
                }

                document.add(table);
                document.close();
                return buildResponse(baos.toByteArray(), "etl_audit_log.pdf", "application/pdf");
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF", e);
            }
        }

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

        return buildResponse(writer.toString().getBytes(), "etl_audit_log.csv", "text/csv");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Build the HTTP response with correct download headers. */
    private ResponseEntity<byte[]> buildResponse(byte[] body, String filename, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
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
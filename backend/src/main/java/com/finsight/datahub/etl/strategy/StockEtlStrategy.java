package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Company;
import com.finsight.datahub.entity.Stock;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.CompanyRepository;
import com.finsight.datahub.repository.StockRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Component
public class StockEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(StockEtlStrategy.class);

    private final StockRepository stockRepository;
    private final CompanyRepository companyRepository;

    public StockEtlStrategy(StockRepository stockRepository, CompanyRepository companyRepository) {
        this.stockRepository = stockRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.STOCK;
    }

    @Override
    @Transactional
    public EtlResult process(InputStream inputStream, UploadHistory uploadHistory) throws Exception {
        EtlResult result = new EtlResult();

        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            int rowNum = 1; // Header is row 1, data starts at row 2
            for (CSVRecord record : parser) {
                rowNum++;
                result.incrementTotal();
                Map<String, String> rowMap = record.toMap();

                try {
                    String dateStr = getRecordValue(record, "date", "trade_date");
                    String symbolStr = getRecordValue(record, "symbol", "ticker");
                    String openStr = getRecordValue(record, "open", "open_price");
                    String highStr = getRecordValue(record, "high", "high_price");
                    String lowStr = getRecordValue(record, "low", "low_price");
                    String closeStr = getRecordValue(record, "close", "close_price");
                    String volumeStr = getRecordValue(record, "volume");
                    String adjCloseStr = getRecordValue(record, "adj_close", "adjusted_close");

                    if (dateStr == null || symbolStr == null || openStr == null || highStr == null
                            || lowStr == null || closeStr == null || volumeStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (date, symbol, open, high, low, close, volume)", rowMap));
                        continue;
                    }

                    LocalDate tradeDate = LocalDate.parse(dateStr);
                    String symbol = symbolStr.toUpperCase().trim();

                    BigDecimal open = new BigDecimal(openStr);
                    BigDecimal high = new BigDecimal(highStr);
                    BigDecimal low = new BigDecimal(lowStr);
                    BigDecimal close = new BigDecimal(closeStr);
                    Long volume = Long.parseLong(volumeStr);
                    BigDecimal adjClose = adjCloseStr != null && !adjCloseStr.isBlank() ? new BigDecimal(adjCloseStr) : close;

                    if (open.compareTo(BigDecimal.ZERO) < 0 || high.compareTo(BigDecimal.ZERO) < 0
                            || low.compareTo(BigDecimal.ZERO) < 0 || close.compareTo(BigDecimal.ZERO) < 0 || volume < 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Negative values not allowed for prices/volume", rowMap));
                        continue;
                    }

                    if (high.compareTo(low) < 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "High price cannot be lower than Low price", rowMap));
                        continue;
                    }

                    // Get or create Company reference
                    Company company = companyRepository.findBySymbol(symbol)
                            .orElseGet(() -> {
                                Company newComp = new Company(symbol, symbol + " Corp", "General", "Equities", "USA", "NASDAQ");
                                return companyRepository.save(newComp);
                            });

                    // Calculate Daily Return relative to previous trading day
                    BigDecimal dailyReturn = null;
                    Optional<Stock> prevStockOpt = stockRepository.findPreviousTradeDateStock(symbol, tradeDate);
                    if (prevStockOpt.isPresent() && prevStockOpt.get().getClosePrice().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal prevClose = prevStockOpt.get().getClosePrice();
                        dailyReturn = close.subtract(prevClose).divide(prevClose, 6, RoundingMode.HALF_UP);
                    }

                    // Upsert existing stock record or create new one
                    Stock stock = stockRepository.findByCompanyAndTradeDate(company, tradeDate)
                            .orElseGet(Stock::new);

                    stock.setCompany(company);
                    stock.setTradeDate(tradeDate);
                    stock.setOpenPrice(open);
                    stock.setHighPrice(high);
                    stock.setLowPrice(low);
                    stock.setClosePrice(close);
                    stock.setAdjClose(adjClose);
                    stock.setVolume(volume);
                    stock.setDailyReturn(dailyReturn);
                    stock.setUpload(uploadHistory);

                    stockRepository.save(stock);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format, expected YYYY-MM-DD: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing row {}", rowNum, e);
                    result.addRejection(new RowValidationErrorDto(rowNum, "Processing error: " + e.getMessage(), rowMap));
                }
            }
        }

        return result;
    }

    private String getRecordValue(CSVRecord record, String... headers) {
        for (String header : headers) {
            if (record.isMapped(header) && record.get(header) != null) {
                return record.get(header).trim();
            }
        }
        return null;
    }
}

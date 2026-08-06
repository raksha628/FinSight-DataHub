package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Etf;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.EtfRepository;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class EtfEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(EtfEtlStrategy.class);

    private final EtfRepository etfRepository;

    public EtfEtlStrategy(EtfRepository etfRepository) {
        this.etfRepository = etfRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.ETF;
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

            int rowNum = 1;
            for (CSVRecord record : parser) {
                rowNum++;
                result.incrementTotal();
                Map<String, String> rowMap = record.toMap();

                try {
                    String symbolStr = getRecordValue(record, "symbol", "ticker");
                    String nameStr = getRecordValue(record, "name", "fund_name");
                    String dateStr = getRecordValue(record, "date", "trade_date");
                    String navStr = getRecordValue(record, "nav", "net_asset_value");
                    String openStr = getRecordValue(record, "open", "open_price");
                    String highStr = getRecordValue(record, "high", "high_price");
                    String lowStr = getRecordValue(record, "low", "low_price");
                    String closeStr = getRecordValue(record, "close", "close_price");
                    String volumeStr = getRecordValue(record, "volume");
                    String aumStr = getRecordValue(record, "aum");
                    String expRatioStr = getRecordValue(record, "expense_ratio", "exp_ratio");
                    String categoryStr = getRecordValue(record, "category");

                    if (symbolStr == null || dateStr == null || navStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (symbol, date, nav)", rowMap));
                        continue;
                    }

                    String symbol = symbolStr.toUpperCase().trim();
                    String name = nameStr != null ? nameStr : symbol + " ETF";
                    LocalDate tradeDate = LocalDate.parse(dateStr);
                    BigDecimal nav = new BigDecimal(navStr);

                    if (nav.compareTo(BigDecimal.ZERO) <= 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "NAV must be positive", rowMap));
                        continue;
                    }

                    Etf etf = etfRepository.findBySymbolAndTradeDate(symbol, tradeDate)
                            .orElseGet(Etf::new);

                    etf.setSymbol(symbol);
                    etf.setName(name);
                    etf.setTradeDate(tradeDate);
                    etf.setNav(nav);
                    if (openStr != null && !openStr.isBlank()) etf.setOpenPrice(new BigDecimal(openStr));
                    if (highStr != null && !highStr.isBlank()) etf.setHighPrice(new BigDecimal(highStr));
                    if (lowStr != null && !lowStr.isBlank()) etf.setLowPrice(new BigDecimal(lowStr));
                    if (closeStr != null && !closeStr.isBlank()) etf.setClosePrice(new BigDecimal(closeStr));
                    if (volumeStr != null && !volumeStr.isBlank()) etf.setVolume(Long.parseLong(volumeStr));
                    if (aumStr != null && !aumStr.isBlank()) etf.setAum(new BigDecimal(aumStr));
                    if (expRatioStr != null && !expRatioStr.isBlank()) etf.setExpenseRatio(new BigDecimal(expRatioStr));
                    if (categoryStr != null) etf.setCategory(categoryStr);
                    etf.setUpload(uploadHistory);

                    etfRepository.save(etf);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format, expected YYYY-MM-DD: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing ETF row {}", rowNum, e);
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

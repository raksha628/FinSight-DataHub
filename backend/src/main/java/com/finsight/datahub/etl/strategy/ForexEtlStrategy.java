package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Forex;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.ForexRepository;
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

@Component
public class ForexEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(ForexEtlStrategy.class);

    private final ForexRepository forexRepository;

    public ForexEtlStrategy(ForexRepository forexRepository) {
        this.forexRepository = forexRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.FOREX;
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
                    String baseCurrency = getRecordValue(record, "base_currency", "base");
                    String quoteCurrency = getRecordValue(record, "quote_currency", "quote");
                    String pairStr = getRecordValue(record, "pair", "symbol");
                    String dateStr = getRecordValue(record, "date", "trade_date");
                    String openStr = getRecordValue(record, "open", "open_rate");
                    String highStr = getRecordValue(record, "high", "high_rate");
                    String lowStr = getRecordValue(record, "low", "low_rate");
                    String closeStr = getRecordValue(record, "close", "close_rate");

                    // Handle FX pair string like "EUR/USD" or "EURUSD" if separate columns are not provided
                    if ((baseCurrency == null || quoteCurrency == null) && pairStr != null) {
                        String cleanPair = pairStr.replace("/", "").replace("-", "").trim();
                        if (cleanPair.length() == 6) {
                            baseCurrency = cleanPair.substring(0, 3);
                            quoteCurrency = cleanPair.substring(3, 6);
                        }
                    }

                    if (baseCurrency == null || quoteCurrency == null || dateStr == null || openStr == null || highStr == null || lowStr == null || closeStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (base_currency, quote_currency, date, open, high, low, close)", rowMap));
                        continue;
                    }

                    baseCurrency = baseCurrency.toUpperCase().trim();
                    quoteCurrency = quoteCurrency.toUpperCase().trim();
                    LocalDate tradeDate = LocalDate.parse(dateStr);
                    BigDecimal open = new BigDecimal(openStr);
                    BigDecimal high = new BigDecimal(highStr);
                    BigDecimal low = new BigDecimal(lowStr);
                    BigDecimal close = new BigDecimal(closeStr);

                    if (open.compareTo(BigDecimal.ZERO) <= 0 || high.compareTo(BigDecimal.ZERO) <= 0
                            || low.compareTo(BigDecimal.ZERO) <= 0 || close.compareTo(BigDecimal.ZERO) <= 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Exchange rates must be strictly positive", rowMap));
                        continue;
                    }

                    BigDecimal dailyChange = null;
                    if (open.compareTo(BigDecimal.ZERO) > 0) {
                        dailyChange = close.subtract(open).divide(open, 6, RoundingMode.HALF_UP);
                    }

                    Forex fx = forexRepository.findByBaseCurrencyAndQuoteCurrencyAndTradeDate(baseCurrency, quoteCurrency, tradeDate)
                            .orElseGet(Forex::new);

                    fx.setBaseCurrency(baseCurrency);
                    fx.setQuoteCurrency(quoteCurrency);
                    fx.setTradeDate(tradeDate);
                    fx.setOpenRate(open);
                    fx.setHighRate(high);
                    fx.setLowRate(low);
                    fx.setCloseRate(close);
                    fx.setDailyChange(dailyChange);
                    fx.setUpload(uploadHistory);

                    forexRepository.saveAndFlush(fx);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing Forex row {}", rowNum, e);
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

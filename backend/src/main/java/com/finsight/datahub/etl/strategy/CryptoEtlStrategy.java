package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.Crypto;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.CryptoRepository;
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
public class CryptoEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(CryptoEtlStrategy.class);

    private final CryptoRepository cryptoRepository;

    public CryptoEtlStrategy(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.CRYPTO;
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
                    String symbolStr = getRecordValue(record, "symbol", "coin");
                    String nameStr = getRecordValue(record, "name");
                    String dateStr = getRecordValue(record, "date", "trade_date");
                    String openStr = getRecordValue(record, "open", "open_price");
                    String highStr = getRecordValue(record, "high", "high_price");
                    String lowStr = getRecordValue(record, "low", "low_price");
                    String closeStr = getRecordValue(record, "close", "close_price");
                    String volumeStr = getRecordValue(record, "volume");
                    String marketCapStr = getRecordValue(record, "market_cap");

                    if (symbolStr == null || dateStr == null || openStr == null || highStr == null || lowStr == null || closeStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (symbol, date, open, high, low, close)", rowMap));
                        continue;
                    }

                    String symbol = symbolStr.toUpperCase().trim();
                    String name = nameStr != null ? nameStr : symbol;
                    LocalDate tradeDate = LocalDate.parse(dateStr);
                    BigDecimal open = new BigDecimal(openStr);
                    BigDecimal high = new BigDecimal(highStr);
                    BigDecimal low = new BigDecimal(lowStr);
                    BigDecimal close = new BigDecimal(closeStr);

                    if (open.compareTo(BigDecimal.ZERO) < 0 || high.compareTo(BigDecimal.ZERO) < 0
                            || low.compareTo(BigDecimal.ZERO) < 0 || close.compareTo(BigDecimal.ZERO) < 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Prices cannot be negative", rowMap));
                        continue;
                    }

                    if (high.compareTo(low) < 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "High price cannot be lower than Low price", rowMap));
                        continue;
                    }

                    BigDecimal dailyReturn = null;
                    if (open.compareTo(BigDecimal.ZERO) > 0) {
                        dailyReturn = close.subtract(open).divide(open, 6, RoundingMode.HALF_UP);
                    }

                    Crypto crypto = cryptoRepository.findBySymbolAndTradeDate(symbol, tradeDate)
                            .orElseGet(Crypto::new);

                    crypto.setSymbol(symbol);
                    crypto.setName(name);
                    crypto.setTradeDate(tradeDate);
                    crypto.setOpenPrice(open);
                    crypto.setHighPrice(high);
                    crypto.setLowPrice(low);
                    crypto.setClosePrice(close);
                    crypto.setDailyReturn(dailyReturn);
                    if (volumeStr != null && !volumeStr.isBlank()) crypto.setVolume(new BigDecimal(volumeStr));
                    if (marketCapStr != null && !marketCapStr.isBlank()) crypto.setMarketCap(new BigDecimal(marketCapStr));
                    crypto.setUpload(uploadHistory);

                    cryptoRepository.save(crypto);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing Crypto row {}", rowNum, e);
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

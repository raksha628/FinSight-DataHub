package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.MutualFund;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.MutualFundRepository;
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
public class MutualFundEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(MutualFundEtlStrategy.class);

    private final MutualFundRepository mutualFundRepository;

    public MutualFundEtlStrategy(MutualFundRepository mutualFundRepository) {
        this.mutualFundRepository = mutualFundRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.MUTUAL_FUND;
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
                    String symbolStr = getRecordValue(record, "symbol", "scheme_code");
                    String nameStr = getRecordValue(record, "name", "scheme_name");
                    String dateStr = getRecordValue(record, "date", "nav_date");
                    String navStr = getRecordValue(record, "nav");
                    String categoryStr = getRecordValue(record, "category");
                    String fundHouseStr = getRecordValue(record, "fund_house", "amc");
                    String aumStr = getRecordValue(record, "aum");
                    String expRatioStr = getRecordValue(record, "expense_ratio");

                    if (symbolStr == null || dateStr == null || navStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (symbol, date, nav)", rowMap));
                        continue;
                    }

                    String symbol = symbolStr.toUpperCase().trim();
                    String name = nameStr != null ? nameStr : symbol + " Mutual Fund";
                    LocalDate navDate = LocalDate.parse(dateStr);
                    BigDecimal nav = new BigDecimal(navStr);

                    if (nav.compareTo(BigDecimal.ZERO) <= 0) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "NAV must be positive", rowMap));
                        continue;
                    }

                    MutualFund mf = mutualFundRepository.findBySymbolAndNavDate(symbol, navDate)
                            .orElseGet(MutualFund::new);

                    mf.setSymbol(symbol);
                    mf.setName(name);
                    mf.setNavDate(navDate);
                    mf.setNav(nav);
                    if (categoryStr != null) mf.setCategory(categoryStr);
                    if (fundHouseStr != null) mf.setFundHouse(fundHouseStr);
                    if (aumStr != null && !aumStr.isBlank()) mf.setAum(new BigDecimal(aumStr));
                    if (expRatioStr != null && !expRatioStr.isBlank()) mf.setExpenseRatio(new BigDecimal(expRatioStr));
                    mf.setUpload(uploadHistory);

                    mutualFundRepository.saveAndFlush(mf);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing Mutual Fund row {}", rowNum, e);
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

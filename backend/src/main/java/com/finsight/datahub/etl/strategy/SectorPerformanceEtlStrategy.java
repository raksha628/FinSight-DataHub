package com.finsight.datahub.etl.strategy;

import com.finsight.datahub.dto.response.RowValidationErrorDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.SectorPerformance;
import com.finsight.datahub.entity.UploadHistory;
import com.finsight.datahub.etl.EtlResult;
import com.finsight.datahub.repository.SectorPerformanceRepository;
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
public class SectorPerformanceEtlStrategy implements EtlStrategy {

    private static final Logger log = LoggerFactory.getLogger(SectorPerformanceEtlStrategy.class);

    private final SectorPerformanceRepository sectorPerformanceRepository;

    public SectorPerformanceEtlStrategy(SectorPerformanceRepository sectorPerformanceRepository) {
        this.sectorPerformanceRepository = sectorPerformanceRepository;
    }

    @Override
    public AssetType getAssetType() {
        return AssetType.SECTOR_PERFORMANCE;
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
                    String sectorStr = getRecordValue(record, "sector", "sector_name");
                    String dateStr = getRecordValue(record, "date", "performance_date");
                    String dailyRetStr = getRecordValue(record, "daily_return_pct", "daily_return");
                    String weeklyRetStr = getRecordValue(record, "weekly_return_pct", "weekly_return");
                    String monthlyRetStr = getRecordValue(record, "monthly_return_pct", "monthly_return");
                    String ytdRetStr = getRecordValue(record, "ytd_return_pct", "ytd_return");
                    String marketCapStr = getRecordValue(record, "total_market_cap", "market_cap");
                    String volumeStr = getRecordValue(record, "total_volume", "volume");
                    String advStr = getRecordValue(record, "advancing_count", "advancing");
                    String decStr = getRecordValue(record, "declining_count", "declining");
                    String uncStr = getRecordValue(record, "unchanged_count", "unchanged");

                    if (sectorStr == null || dateStr == null) {
                        result.addRejection(new RowValidationErrorDto(rowNum, "Missing required fields (sector, date)", rowMap));
                        continue;
                    }

                    String sectorName = sectorStr.trim();
                    LocalDate perfDate = LocalDate.parse(dateStr);

                    SectorPerformance sp = sectorPerformanceRepository.findBySectorNameAndPerformanceDate(sectorName, perfDate)
                            .orElseGet(SectorPerformance::new);

                    sp.setSectorName(sectorName);
                    sp.setPerformanceDate(perfDate);
                    if (dailyRetStr != null && !dailyRetStr.isBlank()) sp.setDailyReturnPct(new BigDecimal(dailyRetStr));
                    if (weeklyRetStr != null && !weeklyRetStr.isBlank()) sp.setWeeklyReturnPct(new BigDecimal(weeklyRetStr));
                    if (monthlyRetStr != null && !monthlyRetStr.isBlank()) sp.setMonthlyReturnPct(new BigDecimal(monthlyRetStr));
                    if (ytdRetStr != null && !ytdRetStr.isBlank()) sp.setYtdReturnPct(new BigDecimal(ytdRetStr));
                    if (marketCapStr != null && !marketCapStr.isBlank()) sp.setTotalMarketCap(new BigDecimal(marketCapStr));
                    if (volumeStr != null && !volumeStr.isBlank()) sp.setTotalVolume(Long.parseLong(volumeStr));
                    if (advStr != null && !advStr.isBlank()) sp.setAdvancingCount(Integer.parseInt(advStr));
                    if (decStr != null && !decStr.isBlank()) sp.setDecliningCount(Integer.parseInt(decStr));
                    if (uncStr != null && !uncStr.isBlank()) sp.setUnchangedCount(Integer.parseInt(uncStr));
                    sp.setUpload(uploadHistory);

                    sectorPerformanceRepository.save(sp);
                    result.incrementAccepted();

                } catch (DateTimeParseException dtpe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid date format: " + dtpe.getMessage(), rowMap));
                } catch (NumberFormatException nfe) {
                    result.addRejection(new RowValidationErrorDto(rowNum, "Invalid numeric format: " + nfe.getMessage(), rowMap));
                } catch (Exception e) {
                    log.error("Unexpected error processing Sector Performance row {}", rowNum, e);
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

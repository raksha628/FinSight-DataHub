package com.finsight.datahub.scheduler;

import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.UploadStatus;
import com.finsight.datahub.service.UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FolderWatcherScheduler {

    private static final Logger log = LoggerFactory.getLogger(FolderWatcherScheduler.class);

    private final UploadService uploadService;

    @Value("${app.etl.incoming-dir:../data/incoming}")
    private String incomingDirStr;

    @Value("${app.etl.archive-dir:../data/archive}")
    private String archiveDirStr;

    @Value("${app.etl.error-dir:../data/error}")
    private String errorDirStr;

    public FolderWatcherScheduler(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Poll incoming directory every 30 seconds for new CSV files.
     */
    @Scheduled(fixedDelayString = "${app.etl.poll-rate-ms:30000}")
    public void scanIncomingFolder() {
        Path incomingPath = Paths.get(incomingDirStr).toAbsolutePath().normalize();
        if (!Files.exists(incomingPath) || !Files.isDirectory(incomingPath)) {
            return;
        }

        File[] files = incomingPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null || files.length == 0) {
            return;
        }

        log.info("Folder Watcher: Found {} CSV file(s) in {}", files.length, incomingPath);

        Path archivePath = Paths.get(archiveDirStr).toAbsolutePath().normalize();
        Path errorPath = Paths.get(errorDirStr).toAbsolutePath().normalize();

        try {
            Files.createDirectories(archivePath);
            Files.createDirectories(errorPath);
        } catch (Exception e) {
            log.error("Failed to create archive/error directories", e);
            return;
        }

        for (File file : files) {
            processIncomingFile(file, archivePath, errorPath);
        }
    }

    private void processIncomingFile(File file, Path archivePath, Path errorPath) {
        String filename = file.getName();
        AssetType assetType = inferAssetType(filename);
        log.info("Automated ETL processing file '{}' inferred as asset type '{}'", filename, assetType);

        try (InputStream is = new FileInputStream(file)) {
            UploadResponseDto result = uploadService.uploadFileStream(is, filename, file.length(), assetType, null);

            if (result.getStatus() == UploadStatus.SUCCESS || result.getStatus() == UploadStatus.PARTIAL) {
                Path dest = archivePath.resolve(System.currentTimeMillis() + "_" + filename);
                Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                log.info("Successfully processed '{}'. Moved to archive.", filename);
            } else {
                Path dest = errorPath.resolve(System.currentTimeMillis() + "_" + filename);
                Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                log.warn("Failed processing '{}'. Moved to error folder.", filename);
            }
        } catch (Exception e) {
            log.error("Error watching/processing file '{}'", filename, e);
            try {
                Path dest = errorPath.resolve(System.currentTimeMillis() + "_" + filename);
                Files.move(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception moveEx) {
                log.error("Could not move failed file '{}' to error folder", filename, moveEx);
            }
        }
    }

    private AssetType inferAssetType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.startsWith("etf") || lower.contains("_etf")) return AssetType.ETF;
        if (lower.startsWith("crypto") || lower.contains("_crypto")) return AssetType.CRYPTO;
        if (lower.startsWith("mutual") || lower.contains("mutual_fund")) return AssetType.MUTUAL_FUND;
        if (lower.startsWith("forex") || lower.contains("_fx")) return AssetType.FOREX;
        if (lower.startsWith("sector") || lower.contains("sector_perf")) return AssetType.SECTOR_PERFORMANCE;
        return AssetType.STOCK;
    }
}

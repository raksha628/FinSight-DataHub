package com.finsight.datahub.service;

import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface UploadService {

    UploadResponseDto uploadCsv(MultipartFile file, AssetType assetType, User user);

    UploadResponseDto uploadFileStream(InputStream inputStream, String originalFilename, long fileSizeBytes, AssetType assetType, User user);

    Page<UploadHistoryDto> getUploadHistory(Pageable pageable);

    UploadHistoryDto getUploadDetails(Long id);
}

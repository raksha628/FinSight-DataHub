package com.finsight.datahub.service;

import com.finsight.datahub.dto.response.UploadHistoryDto;
import com.finsight.datahub.dto.response.UploadResponseDto;
import com.finsight.datahub.entity.AssetType;
import com.finsight.datahub.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface UploadService {

    UploadResponseDto uploadCsv(MultipartFile file, AssetType assetType, User user);

    UploadResponseDto uploadFileStream(InputStream inputStream, String originalFilename, long fileSizeBytes, AssetType assetType, User user);

    List<UploadHistoryDto> getUploadHistory();

    UploadHistoryDto getUploadDetails(Long id);
}

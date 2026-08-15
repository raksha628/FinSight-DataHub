package com.finsight.datahub.repository;

import com.finsight.datahub.entity.UploadHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Long> {
    List<UploadHistory> findAllByOrderByUploadedAtDesc();
    Page<UploadHistory> findAllByOrderByUploadedAtDesc(Pageable pageable);
}

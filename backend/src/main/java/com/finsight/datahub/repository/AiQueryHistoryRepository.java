package com.finsight.datahub.repository;

import com.finsight.datahub.entity.AiQueryHistory;
import com.finsight.datahub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiQueryHistoryRepository extends JpaRepository<AiQueryHistory, Long> {
    List<AiQueryHistory> findByUserOrderByCreatedAtDesc(User user);
    List<AiQueryHistory> findAllByOrderByCreatedAtDesc();
}

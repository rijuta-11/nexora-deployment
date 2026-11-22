package com.nexora.repository;

import com.nexora.entity.AIInteractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIInteractionLogRepository extends JpaRepository<AIInteractionLog, Long> {
    List<AIInteractionLog> findByAnalysisIdOrderByCreatedAtAsc(String analysisId);
}

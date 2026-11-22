package com.nexora.repository;

import com.nexora.entity.RecommendationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationResultRepository extends JpaRepository<RecommendationResult, Long> {
    List<RecommendationResult> findByAnalysisRunIdOrderByRankOrderAsc(String analysisRunId);
    List<RecommendationResult> findByAnalysisRunIdAndInstrumentIdIn(String analysisRunId, List<Long> instrumentIds);
}

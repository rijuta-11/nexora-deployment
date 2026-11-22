package com.nexora.repository;

import com.nexora.entity.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, String> {
    List<AnalysisRun> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<AnalysisRun> findByIdAndUserId(String id, Long userId);
}

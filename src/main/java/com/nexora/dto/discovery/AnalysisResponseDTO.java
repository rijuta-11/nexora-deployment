package com.nexora.dto.discovery;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisResponseDTO {
    private String analysisId;
    private LocalDateTime generatedAt;
    private String marketDataAsOf;
    private String scoringVersion;
    private PortfolioSummaryDTO portfolioSummary;
    private List<RecommendationDTO> recommendations;
    private Integer totalCandidatesEvaluated;
}

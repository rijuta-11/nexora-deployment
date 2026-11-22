package com.nexora.dto.discovery;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockCompareResponseDTO {
    private String analysisId;
    private List<RecommendationDTO> candidates;
    private Map<String, Object> comparisonMatrix;
    private List<String> keyDifferences;
    private String comparisonSummary;
    private String dataSnapshotAsOf;
}

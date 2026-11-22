package com.nexora.dto.discovery;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ScoreBreakdownDTO {
    private Integer quality;        // max 25
    private Integer valuation;      // max 20
    private Integer growth;         // max 15
    private Integer risk;           // max 15
    private Integer industryFit;    // max 10
    private Integer portfolioFit;   // max 15
    private Integer totalScore;     // max 100
    private String scoringVersion;  // v1.0
}

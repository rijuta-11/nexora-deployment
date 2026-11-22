package com.nexora.dto.discovery;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendationDTO {
    private Long instrumentId;
    private String symbol;
    private String companyName;
    private String exchange;
    private String industry;
    private String sector;
    private Integer rank;
    private Integer score;
    private String scoreCategory;       // Strong Match (80-100), Reasonable Match (65-79), Lower Match (<65)
    private ScoreBreakdownDTO scoreBreakdown;
    private String portfolioFit;        // e.g. "Useful for diversification", "High sector concentration"
    private List<String> reasons;
    private List<String> risks;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private Map<String, Object> keyMetrics; // ROE, P/E, Debt/Equity, Rev Growth, etc.
    private String dataAsOf;
    private AiExplanationDTO aiExplanation;
}

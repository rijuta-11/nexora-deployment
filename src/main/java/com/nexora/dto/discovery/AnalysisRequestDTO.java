package com.nexora.dto.discovery;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisRequestDTO {
    private String industry;
    private List<String> industries;
    private String market;

    @JsonAlias({"risk_preference", "riskPreference"})
    private String riskPreference;      // LOWER, MODERATE, HIGHER

    @JsonAlias({"investment_horizon", "investmentHorizon"})
    private String investmentHorizon;   // SHORT_TERM, MEDIUM_TERM, LONG_TERM

    @JsonAlias({"investment_goal", "investmentGoal"})
    private String investmentGoal;      // GROWTH, STABILITY, INCOME, BALANCED_GROWTH

    @JsonAlias({"budget_range", "budgetRange"})
    private String budgetRange;

    @JsonAlias({"portfolio_source", "portfolioSource"})
    private String portfolioSource;     // NEXORA_PORTFOLIO, MANUAL, NONE
}

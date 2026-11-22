package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecommendationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisRun analysisRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "quality_score")
    private Integer qualityScore;

    @Column(name = "valuation_score")
    private Integer valuationScore;

    @Column(name = "growth_score")
    private Integer growthScore;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "industry_fit_score")
    private Integer industryFitScore;

    @Column(name = "portfolio_fit_score")
    private Integer portfolioFitScore;

    @Column(name = "portfolio_fit_label", length = 100)
    private String portfolioFitLabel;

    @Column(name = "reasons_json", columnDefinition = "LONGTEXT")
    private String reasonsJson;

    @Column(name = "risks_json", columnDefinition = "LONGTEXT")
    private String risksJson;

    @Column(name = "score_breakdown_json", columnDefinition = "LONGTEXT")
    private String scoreBreakdownJson;

    @Column(name = "data_quality_flags_json", columnDefinition = "LONGTEXT")
    private String dataQualityFlagsJson;

    @Column(name = "ai_explanation_json", columnDefinition = "LONGTEXT")
    private String aiExplanationJson;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

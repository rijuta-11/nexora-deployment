package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_runs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalysisRun {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "input_snapshot", columnDefinition = "LONGTEXT")
    private String inputSnapshot;

    @Column(name = "scoring_version", length = 20)
    @Builder.Default
    private String scoringVersion = "v1.0";

    @Column(name = "status", length = 30)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "portfolio_summary", columnDefinition = "LONGTEXT")
    private String portfolioSummary;

    @Column(name = "market_data_as_of")
    private LocalDateTime marketDataAsOf;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

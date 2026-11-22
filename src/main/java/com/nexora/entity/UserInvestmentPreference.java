package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_investment_preferences")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserInvestmentPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "industries", length = 500)
    private String industries;

    @Column(name = "risk_preference", length = 30)
    private String riskPreference;

    @Column(name = "investment_horizon", length = 30)
    private String investmentHorizon;

    @Column(name = "investment_goal", length = 50)
    private String investmentGoal;

    @Column(name = "budget_range", length = 50)
    private String budgetRange;

    @Column(name = "portfolio_source", length = 50)
    private String portfolioSource;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}

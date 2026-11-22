package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_interaction_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIInteractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "analysis_id", length = 64)
    private String analysisId;

    @Column(name = "prompt_version", length = 30)
    private String promptVersion;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "user_question", columnDefinition = "LONGTEXT")
    private String userQuestion;

    @Column(name = "ai_response", columnDefinition = "LONGTEXT")
    private String aiResponse;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(length = 30)
    private String status;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

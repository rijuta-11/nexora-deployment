package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_data_snapshots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketDataSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "metric_key", nullable = false, length = 50)
    private String metricKey;

    @Column(name = "metric_value", precision = 14, scale = 4)
    private BigDecimal metricValue;

    @Column(length = 20)
    private String unit;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(length = 100)
    private String source;

    @Column(name = "as_of")
    private LocalDate asOf;

    @Column(name = "retrieved_at")
    @Builder.Default
    private LocalDateTime retrievedAt = LocalDateTime.now();
}

package com.nexora.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "instruments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String exchange = "NSE";

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(length = 50)
    private String sector;

    @Column(nullable = false, length = 80)
    private String industry;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "current_price", precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "market_cap", precision = 18, scale = 2)
    private BigDecimal marketCap;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_status", nullable = false)
    @Builder.Default
    private InstrumentStatus instrumentStatus = InstrumentStatus.ACTIVE;

    @Column(name = "data_as_of")
    private LocalDate dataAsOf;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum InstrumentStatus { ACTIVE, SUSPENDED, DELISTED }
}

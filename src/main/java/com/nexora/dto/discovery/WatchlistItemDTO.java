package com.nexora.dto.discovery;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchlistItemDTO {
    private Long id;
    private Long instrumentId;
    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private String industry;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private String notes;
    private String dataAsOf;
    private LocalDateTime addedAt;
}

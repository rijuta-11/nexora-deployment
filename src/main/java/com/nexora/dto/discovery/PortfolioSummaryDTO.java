package com.nexora.dto.discovery;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioSummaryDTO {
    private BigDecimal totalPortfolioValue;
    private Integer totalHoldings;
    private List<SectorExposureDTO> sectorExposure;
    private List<String> warnings;
    private String topSector;
    private Double topSectorPct;
}

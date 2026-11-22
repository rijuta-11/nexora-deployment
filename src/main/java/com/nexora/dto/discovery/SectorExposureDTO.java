package com.nexora.dto.discovery;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SectorExposureDTO {
    private String sector;
    private Double weightPct;
    private BigDecimal totalValue;
    private Integer holdingCount;
}

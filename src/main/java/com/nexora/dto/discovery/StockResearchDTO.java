package com.nexora.dto.discovery;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockResearchDTO {
    private Long id;
    private String symbol;
    private String companyName;
    private String exchange;
    private String sector;
    private String industry;
    private String description;
    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private String dataAsOf;
    private Map<String, Object> fundamentals; // P/E, ROE, etc.
    private List<Map<String, Object>> metricsHistory;
    private List<String> strengthHighlights;
    private List<String> riskWarnings;
    private String source;
}

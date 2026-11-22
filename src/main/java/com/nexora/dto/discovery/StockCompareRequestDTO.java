package com.nexora.dto.discovery;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockCompareRequestDTO {
    @JsonAlias({"analysis_id", "analysisId"})
    private String analysisId;

    private List<String> symbols;
}

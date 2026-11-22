package com.nexora.dto.discovery;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AiExplanationDTO {
    private String plainLanguageSummary;
    private List<String> whyItMatches;
    private List<String> risksToConsider;
    private String portfolioImpact;
    private List<String> dataLimitations;
    private String disclaimer;
}

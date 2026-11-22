package com.nexora.dto.discovery;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AiChatRequestDTO {
    @JsonAlias({"analysis_id", "analysisId"})
    private String analysisId;

    private String question;

    @JsonAlias({"focused_symbol", "focusedSymbol"})
    private String focusedSymbol;
}

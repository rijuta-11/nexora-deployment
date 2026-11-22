package com.nexora.dto.discovery;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AiChatResponseDTO {
    private String answer;
    private List<String> citations;
    private String dataAsOf;
    private List<String> suggestedQuestions;
    private String disclaimer;
    private LocalDateTime timestamp;
}

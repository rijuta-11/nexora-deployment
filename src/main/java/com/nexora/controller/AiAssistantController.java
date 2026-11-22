package com.nexora.controller;

import com.nexora.dto.ApiResponse;
import com.nexora.dto.discovery.AiChatRequestDTO;
import com.nexora.dto.discovery.AiChatResponseDTO;
import com.nexora.service.AiDiscoveryService;
import com.nexora.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiDiscoveryService aiDiscoveryService;
    private final UserService userService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponseDTO>> chat(
            @RequestBody AiChatRequestDTO request,
            Authentication auth) {
        Long userId = userService.getUserByEmail(auth.getName()).getId();
        AiChatResponseDTO response = aiDiscoveryService.chatAssistant(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.taskportal.controller;

import com.taskportal.dto.ai.AiGenerateRequest;
import com.taskportal.dto.ai.AiGenerateResponse;
import com.taskportal.service.AiTaskGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Automation")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiTaskGeneratorService aiService;

    public AiController(AiTaskGeneratorService aiService) {
        this.aiService = aiService;
    }

    /**
     * Generate task description, priority, and effort from a title.
     * Optionally pass "provider": "openai" | "gemini" | "huggingface" | "auto"
     */
    @PostMapping("/generate-task")
    @Operation(summary = "Generate task description, priority, and effort from title")
    public AiGenerateResponse generate(@Valid @RequestBody AiGenerateRequest request) {
        return aiService.generateFromTitle(request.getTitle(), request.getProvider());
    }

    /**
     * Returns which AI providers are configured (have API keys set).
     * The frontend uses this to show/hide provider options.
     */
    @GetMapping("/providers")
    @Operation(summary = "Get configured AI provider status")
    public Map<String, Object> providers() {
        return aiService.getProviderStatus();
    }
}

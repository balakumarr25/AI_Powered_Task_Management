package com.taskportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskportal.config.AppProperties;
import com.taskportal.dto.ai.AiGenerateResponse;
import com.taskportal.entity.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * AI Task Description Generator supporting OpenAI, Gemini, and Hugging Face.
 * Provider selection order: explicit request param → app.ai.provider config → auto-detect by key.
 */
@Service
public class AiTaskGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskGeneratorService.class);

    private final AppProperties props;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiTaskGeneratorService(AppProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Generate with auto provider selection. */
    public AiGenerateResponse generateFromTitle(String title) {
        return generateFromTitle(title, null);
    }

    /**
     * Generate with an explicit provider override.
     * @param provider one of "openai", "gemini", "huggingface", "auto", or null
     */
    public AiGenerateResponse generateFromTitle(String title, String provider) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty()) {
            return AiGenerateResponse.of("Enter a task title first.", TaskPriority.MEDIUM, "—", true, "Title required");
        }

        String resolved = resolveProvider(provider);
        log.info("AI provider resolved to '{}' for title: {}", resolved, trimmed);

        try {
            return switch (resolved) {
                case "openai"      -> callOpenAi(trimmed);
                case "gemini"      -> callGemini(trimmed);
                case "huggingface" -> callHuggingFace(trimmed);
                default            -> fallbackGenerate(trimmed);
            };
        } catch (Exception ex) {
            log.warn("AI provider '{}' failed ({}), falling back to template.", resolved, ex.getMessage());
            return fallbackGenerate(trimmed);
        }
    }

    /** Returns which providers are currently configured (have API keys). */
    public Map<String, Object> getProviderStatus() {
        return Map.of(
            "openai",      hasOpenAiKey(),
            "gemini",      hasGeminiKey(),
            "huggingface", hasHuggingFaceKey(),
            "configured",  resolveProvider(null),
            "models", Map.of(
                "openai",      props.getAi().getOpenaiModel(),
                "gemini",      props.getAi().getGeminiModel(),
                "huggingface", props.getAi().getHuggingfaceModel()
            )
        );
    }

    // ── Provider resolution ───────────────────────────────────────────────────

    private String resolveProvider(String requested) {
        // 1. Explicit request param wins
        if (requested != null && !requested.isBlank() && !requested.equalsIgnoreCase("auto")) {
            return requested.toLowerCase();
        }
        // 2. Config-level preference (if not "auto")
        String configured = props.getAi().getProvider();
        if (configured != null && !configured.isBlank() && !configured.equalsIgnoreCase("auto")) {
            return configured.toLowerCase();
        }
        // 3. Auto-detect: first key found
        if (hasOpenAiKey())      return "openai";
        if (hasGeminiKey())      return "gemini";
        if (hasHuggingFaceKey()) return "huggingface";
        return "fallback";
    }

    private boolean hasOpenAiKey() {
        String k = props.getAi().getOpenaiApiKey();
        return k != null && !k.isBlank();
    }

    private boolean hasGeminiKey() {
        String k = props.getAi().getGeminiApiKey();
        return k != null && !k.isBlank();
    }

    private boolean hasHuggingFaceKey() {
        String k = props.getAi().getHuggingfaceApiKey();
        return k != null && !k.isBlank();
    }

    // ── OpenAI ────────────────────────────────────────────────────────────────

    private AiGenerateResponse callOpenAi(String title) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
            "model", props.getAi().getOpenaiModel(),
            "messages", new Object[]{
                Map.of("role", "system", "content", systemInstructions()),
                Map.of("role", "user",   "content", buildPrompt(title))
            },
            "temperature", 0.75,
            "max_tokens",  1000
        ));

        String response = restClient.post()
            .uri("https://api.openai.com/v1/chat/completions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getAi().getOpenaiApiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        JsonNode root    = objectMapper.readTree(response);
        String   content = root.path("choices").path(0).path("message").path("content").asText();
        return parseAiJson(title, content, false,
            "✦ Generated via OpenAI (" + props.getAi().getOpenaiModel() + ")");
    }

    // ── Gemini ────────────────────────────────────────────────────────────────

    private AiGenerateResponse callGemini(String title) throws Exception {
        String model = props.getAi().getGeminiModel();
        String url   = "https://generativelanguage.googleapis.com/v1beta/models/"
                     + model + ":generateContent?key=" + props.getAi().getGeminiApiKey();

        String body = objectMapper.writeValueAsString(Map.of(
            "contents", new Object[]{
                Map.of("parts", new Object[]{
                    Map.of("text", systemInstructions() + "\n\n" + buildPrompt(title))
                })
            },
            "generationConfig", Map.of("temperature", 0.75, "maxOutputTokens", 1000)
        ));

        String response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        JsonNode root    = objectMapper.readTree(response);
        String   content = root.path("candidates").path(0)
                              .path("content").path("parts").path(0)
                              .path("text").asText();
        return parseAiJson(title, content, false,
            "✦ Generated via Gemini (" + model + ")");
    }

    // ── Hugging Face ──────────────────────────────────────────────────────────

    /**
     * Calls the HF Inference Router (router.huggingface.co/v1/chat/completions).
     * Model format: "org/model:provider" — we append ":together" as the default
     * free provider. If the configured model already contains ":" we use it as-is.
     */
    private AiGenerateResponse callHuggingFace(String title) throws Exception {
        String configuredModel = props.getAi().getHuggingfaceModel();
        // Append default provider if not already specified
        String model = configuredModel.contains(":") ? configuredModel : configuredModel + ":together";

        String prompt = systemInstructions() + "\n\n" + buildPrompt(title);

        String body = objectMapper.writeValueAsString(Map.of(
            "model",       model,
            "messages",    List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens",  1000,
            "temperature", 0.75,
            "stream",      false
        ));

        String response = restClient.post()
            .uri("https://router.huggingface.co/v1/chat/completions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getAi().getHuggingfaceApiKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        JsonNode root    = objectMapper.readTree(response);
        String   content = root.path("choices").path(0).path("message").path("content").asText();
        return parseAiJson(title, content, false,
            "✦ Generated via Hugging Face (" + configuredModel + ")");
    }

    // ── Prompt helpers ────────────────────────────────────────────────────────

    private static String systemInstructions() {
        return """
            You are an expert coach who writes UNIQUE preparation guides per task.
            NEVER reuse the same generic template for different tasks.
            FORBIDDEN phrases unless truly relevant: "gather stakeholders", "break into milestones",
            "stakeholder communication", "quality and clarity over speed".
            Every bullet must relate to the exact task title and its domain (tech, study, design, etc.).
            Respond ONLY with valid JSON. No markdown code fences.
            """;
    }

    private String buildPrompt(String title) {
        String taskType = suggestTaskTypeHint(title);
        return """
            Task title: "%s"
            Detected domain hint: %s

            Write a preparation guide ONLY for this specific task.

            Return JSON:
            {
              "description": "150-250 words. Use a structure that fits THIS task. Include 5-8 specific bullets mentioning tools, artifacts, or skills relevant to '%s'.",
              "suggestedPriority": "LOW|MEDIUM|HIGH",
              "estimatedEffort": "realistic range for this exact work"
            }

            Rules:
            - Content must be different if the title changes even slightly
            - Be practical and specific, not corporate boilerplate
            """.formatted(title, taskType, title);
    }

    private static String suggestTaskTypeHint(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("presentation") || lower.contains("pitch")) return "presentation / public speaking";
        if (lower.contains("code") || lower.contains("api") || lower.contains("bug")) return "software development";
        if (lower.contains("exam") || lower.contains("study"))  return "learning / examination";
        if (lower.contains("design") || lower.contains("ui"))   return "product design";
        if (lower.contains("email"))    return "written communication";
        if (lower.contains("meeting"))  return "meetings / facilitation";
        if (lower.contains("interview")) return "hiring / career";
        if (lower.contains("marketing") || lower.contains("campaign")) return "marketing";
        return "general work — infer domain from title keywords";
    }

    // ── Response parsing ──────────────────────────────────────────────────────

    private AiGenerateResponse parseAiJson(String title, String content, boolean fallback, String message)
            throws Exception {
        String json = content.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("```json?", "").replace("```", "").trim();
        }
        JsonNode node = objectMapper.readTree(json);
        TaskPriority priority = TaskPriority.valueOf(
            node.path("suggestedPriority").asText("MEDIUM").toUpperCase()
        );
        String description = node.path("description").asText();
        if (description.length() < 80) {
            TaskBriefTemplates.BriefResult enriched = TaskBriefTemplates.build(title);
            description = enriched.description();
            fallback = true;
            message  = "AI response too short — using task-specific prep guide";
        }
        return AiGenerateResponse.of(
            description, priority,
            node.path("estimatedEffort").asText("2 hours"),
            fallback, message
        );
    }

    private AiGenerateResponse fallbackGenerate(String title) {
        TaskBriefTemplates.BriefResult brief = TaskBriefTemplates.build(title);
        return AiGenerateResponse.of(
            brief.description(), brief.priority(), brief.effort(),
            true, "Smart prep guide matched to your task type (add an API key for live AI)"
        );
    }
}

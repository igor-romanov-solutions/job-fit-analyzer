package io.github.igorromanovsolutions.jobfitanalyzer.ai;

import io.github.igorromanovsolutions.jobfitanalyzer.config.OpenAiProperties;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.ExternalServiceException;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiClient {

    private static final String MODEL = "gpt-4o-mini";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    public AnalysisResult callOpenAi(String systemPrompt, String userPrompt) {

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );

        String correlationId = getCorrelationId();
        int payloadLength = request.toString().length();

        log.info("Sending request to OpenAI: model={}, payloadLength={}, correlationId={}",
                MODEL, payloadLength, correlationId);

        try {
            String rawResponse = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openAiProperties.apiKey())
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new ExternalServiceException("OpenAI returned empty response");
            }

            return parseOpenAiResponse(rawResponse);
        } catch (Exception ex) {
            log.error("OpenAI request failed: correlationId={}", correlationId, ex);
            throw new ExternalServiceException("OpenAI request failed: " + ex.getMessage());
        }
    }

    private AnalysisResult parseOpenAiResponse(String rawResponse) {
        try {
            OpenAiResponse openAiResponse = objectMapper.readValue(rawResponse, OpenAiResponse.class);

            if (openAiResponse.getChoices() == null || openAiResponse.getChoices().isEmpty()) {
                throw new ExternalServiceException("OpenAI response has no choices");
            }

            String contentJson = openAiResponse.getChoices().getFirst().getMessage().getContent();

            if (contentJson == null || contentJson.isBlank()) {
                throw new ExternalServiceException("OpenAI response content is empty");
            }

            contentJson = contentJson.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(contentJson, AnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", rawResponse, e);
            throw new ExternalServiceException("Failed to parse OpenAI response: " + e.getMessage());
        }
    }

    private String getCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId != null ? correlationId : "n/a";
    }
}
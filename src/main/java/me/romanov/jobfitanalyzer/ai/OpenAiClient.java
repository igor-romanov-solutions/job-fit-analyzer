package me.romanov.jobfitanalyzer.ai;

import lombok.RequiredArgsConstructor;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.OpenAiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient restClient;

    @Value("${openai.api.key}")
    private String apiKey;

    public AnalysisResult callOpenAi(String systemPrompt, String userPrompt) {

        Map<String, Object> request = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object")
        );

        System.out.println("Sending request to OpenAI: " + request);

        String rawResponse = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(String.class);
        return parseOpenAiResponse(rawResponse);
    }

    private AnalysisResult parseOpenAiResponse(String rawResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            OpenAiResponse openAiResponse = mapper.readValue(rawResponse, OpenAiResponse.class);
            String contentJson = openAiResponse.getChoices().get(0).getMessage().getContent();
            contentJson = contentJson.replace("```json", "").replace("```", "").trim();
            return mapper.readValue(contentJson, AnalysisResult.class);
        } catch (Exception e) {
            System.err.println("Failed to parse OpenAI response: {}" + rawResponse + e);
            return AnalysisResult.fallback();
        }
    }
}
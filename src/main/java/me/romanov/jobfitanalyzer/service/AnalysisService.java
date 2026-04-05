package me.romanov.jobfitanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAiClient openAiClient;

    @PostConstruct
    public void test() {
        System.out.println("API Key loaded: " + apiKey);
    }

    public AnalysisResult analyze(String candidateProfile, String vacancyText) {
        String systemPrompt = PromptBuilder.buildSystemPrompt();
        String userPrompt = PromptBuilder.buildUserPrompt(candidateProfile, vacancyText);
        return openAiClient.callOpenAi(systemPrompt, userPrompt);
    }
}

package me.romanov.jobfitanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.romanov.jobfitanalyzer.ai.OpenAiClient;
import me.romanov.jobfitanalyzer.ai.PromptBuilder;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import me.romanov.jobfitanalyzer.mapper.AnalysisMapper;
import me.romanov.jobfitanalyzer.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAiClient openAiClient;
    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;

    /*public AnalysisServiceImpl(OpenAiClient openAiClient,
                               AnalysisRepository analysisRepository,
                               AnalysisMapper analysisMapper) {
        this.openAiClient = openAiClient;
        this.analysisRepository = analysisRepository;
        this.analysisMapper = analysisMapper;
    }*/

    @PostConstruct
    public void test() {
        System.out.println("API Key loaded: " + apiKey);
    }

    @Override
    public AnalysisResult analyzeAndSave(AnalysisRequest request) {
        String systemPrompt = PromptBuilder.buildSystemPrompt();
        String userPrompt = PromptBuilder.buildUserPrompt(request.getCandidateProfile(), request.getVacancyText());
        AnalysisResult result = openAiClient.callOpenAi(systemPrompt, userPrompt);
        AnalysisEntity entity = analysisMapper.toEntity(request, result);
        analysisRepository.save(entity);
        return result;
    }
}

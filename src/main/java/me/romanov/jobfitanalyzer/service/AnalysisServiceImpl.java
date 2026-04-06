package me.romanov.jobfitanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.romanov.jobfitanalyzer.ai.OpenAiClient;
import me.romanov.jobfitanalyzer.ai.PromptBuilder;
import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import me.romanov.jobfitanalyzer.mapper.AnalysisMapper;
import me.romanov.jobfitanalyzer.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAiClient openAiClient;
    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;

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

    @Override
    public List<AnalysisHistoryItemDto> getHistory() {
        return analysisRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(analysisMapper::toHistoryItemDto)
                .toList();
    }

    @Override
    public AnalysisViewDto getAnalysis(Long id) {
        AnalysisEntity entity = analysisRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found: " + id));

        return analysisMapper.toViewDto(entity);
    }
}

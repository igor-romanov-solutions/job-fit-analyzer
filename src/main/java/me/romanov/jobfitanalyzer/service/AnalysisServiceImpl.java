package me.romanov.jobfitanalyzer.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.romanov.jobfitanalyzer.ai.OpenAiClient;
import me.romanov.jobfitanalyzer.ai.PromptBuilder;
import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.mapper.AnalysisMapper;
import me.romanov.jobfitanalyzer.repository.JobAnalysisRepository;
import me.romanov.jobfitanalyzer.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAiClient openAiClient;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AnalysisMapper analysisMapper;

    @PostConstruct
    public void test() {
        log.info("API Key loaded: {}", apiKey);
    }

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        String systemPrompt = PromptBuilder.buildSystemPrompt();
        String userPrompt = PromptBuilder.buildUserPrompt(request.getCandidateProfile(), request.getJobPostingDescription());
        return openAiClient.callOpenAi(systemPrompt, userPrompt);
    }

    @Override
    public JobAnalysis analyzeAndSave(JobPosting jobPosting, AnalysisRequest request) {
        AnalysisResult analysisResult = analyze(request);
        JobAnalysis entity = analysisMapper.toEntity(jobPosting, analysisResult);
        jobAnalysisRepository.save(entity);
        jobPosting.setStatus(JobPostingStatus.ANALYZED);
        jobPostingRepository.save(jobPosting);
        return entity;
    }
}

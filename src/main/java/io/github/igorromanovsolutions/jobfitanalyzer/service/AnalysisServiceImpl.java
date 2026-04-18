package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.ai.OpenAiClient;
import io.github.igorromanovsolutions.jobfitanalyzer.ai.PromptBuilder;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.*;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisRequest;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;
import io.github.igorromanovsolutions.jobfitanalyzer.mapper.AnalysisMapper;
import io.github.igorromanovsolutions.jobfitanalyzer.repository.JobAnalysisRepository;
import io.github.igorromanovsolutions.jobfitanalyzer.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final OpenAiClient openAiClient;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;
    private final AnalysisMapper analysisMapper;

    @Override
    public AnalysisResult analyze(AnalysisRequest request) {
        String systemPrompt = PromptBuilder.buildSystemPrompt();
        String userPrompt = PromptBuilder.buildUserPrompt(
                request.getCandidateProfile(),
                request.getJobPostingDescription()
        );
        return openAiClient.callOpenAi(systemPrompt, userPrompt);
    }

    @Override
    @Transactional
    public JobAnalysis analyzeAndSave(JobPosting jobPosting, AnalysisRequest request) {
        try {
            AnalysisResult analysisResult = analyze(request);
            JobAnalysis entity = analysisMapper.toEntity(jobPosting, analysisResult);

            jobAnalysisRepository.save(entity);
            jobPosting.setStatus(JobPostingStatus.ANALYZED);
            jobPostingRepository.save(jobPosting);

            return entity;
        } catch (ExternalServiceException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            log.error("Failed to persist analysis results for jobPostingId={}", jobPosting.getId(), ex);
            throw new AnalysisFailedException("Failed to save analysis results");
        } catch (Exception ex) {
            log.error("Analysis process failed for jobPostingId={}", jobPosting.getId(), ex);
            throw new AnalysisFailedException("Analysis process failed: " + ex.getMessage());
        }
    }
}
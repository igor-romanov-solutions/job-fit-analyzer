package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.ai.OpenAiClient;
import io.github.igorromanovsolutions.jobfitanalyzer.ai.PromptBuilder;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.*;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisRequest;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;
import io.github.igorromanovsolutions.jobfitanalyzer.mapper.AnalysisMapper;
import io.github.igorromanovsolutions.jobfitanalyzer.repository.JobAnalysisRepository;
import io.github.igorromanovsolutions.jobfitanalyzer.repository.JobPostingRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private JobAnalysisRepository jobAnalysisRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private AnalysisMapper analysisMapper;

    @Nested
    class AnalyzeTests {

        @Test
        void shouldCallOpenAiWithBuiltPromptsAndReturnResult() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer with Spring");
            request.setJobPostingDescription("Senior Java Backend role");

            AnalysisResult expectedResult = new AnalysisResult();
            expectedResult.setRoleType("BACKEND");

            String expectedSystemPrompt = PromptBuilder.buildSystemPrompt();
            String expectedUserPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(expectedSystemPrompt, expectedUserPrompt))
                    .thenReturn(expectedResult);

            AnalysisResult actualResult = service.analyze(request);

            assertSame(expectedResult, actualResult);

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verifyNoInteractions(jobAnalysisRepository, analysisMapper, jobPostingRepository);
        }

        @Test
        void shouldPropagateExternalServiceExceptionWhenOpenAiFails() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer");
            request.setJobPostingDescription("Backend role");

            String systemPrompt = PromptBuilder.buildSystemPrompt();
            String userPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(systemPrompt, userPrompt))
                    .thenThrow(new ExternalServiceException("OpenAI error"));

            ExternalServiceException exception = assertThrows(
                    ExternalServiceException.class,
                    () -> service.analyze(request)
            );

            assertEquals("OpenAI error", exception.getMessage());

            verify(openAiClient).callOpenAi(systemPrompt, userPrompt);
            verifyNoInteractions(jobAnalysisRepository, analysisMapper, jobPostingRepository);
        }
    }

    @Nested
    class AnalyzeAndSaveTests {

        @Test
        void shouldAnalyzeMapAndSaveEntity() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer with Spring");
            request.setJobPostingDescription("Senior Java Backend role");

            JobPosting jobPosting = new JobPosting(
                    "https://example.com",
                    "Company",
                    "Title",
                    "Location",
                    "Description"
            );

            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setRoleType("BACKEND");

            JobAnalysis mappedEntity = new JobAnalysis();

            String expectedSystemPrompt = PromptBuilder.buildSystemPrompt();
            String expectedUserPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(expectedSystemPrompt, expectedUserPrompt))
                    .thenReturn(analysisResult);
            when(analysisMapper.toEntity(jobPosting, analysisResult))
                    .thenReturn(mappedEntity);
            when(jobAnalysisRepository.save(mappedEntity))
                    .thenReturn(mappedEntity);
            when(jobPostingRepository.save(jobPosting))
                    .thenReturn(jobPosting);

            JobAnalysis actualResult = service.analyzeAndSave(jobPosting, request);

            assertSame(mappedEntity, actualResult);
            assertEquals(JobPostingStatus.ANALYZED, jobPosting.getStatus());

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verify(analysisMapper).toEntity(jobPosting, analysisResult);
            verify(jobAnalysisRepository).save(mappedEntity);
            verify(jobPostingRepository).save(jobPosting);
            verifyNoMoreInteractions(openAiClient, analysisMapper, jobAnalysisRepository, jobPostingRepository);
        }

        @Test
        void shouldPropagateExternalServiceExceptionWhenOpenAiFails() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer");
            request.setJobPostingDescription("Backend role");

            JobPosting jobPosting = new JobPosting(
                    "https://example.com",
                    "Company",
                    "Title",
                    "Location",
                    "Description"
            );

            String systemPrompt = PromptBuilder.buildSystemPrompt();
            String userPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(systemPrompt, userPrompt))
                    .thenThrow(new ExternalServiceException("OpenAI error"));

            ExternalServiceException exception = assertThrows(
                    ExternalServiceException.class,
                    () -> service.analyzeAndSave(jobPosting, request)
            );

            assertEquals("OpenAI error", exception.getMessage());

            verify(openAiClient).callOpenAi(systemPrompt, userPrompt);
            verifyNoInteractions(analysisMapper, jobAnalysisRepository, jobPostingRepository);
        }

        @Test
        void shouldWrapSaveFailureIntoAnalysisFailedException() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer");
            request.setJobPostingDescription("Backend role");

            JobPosting jobPosting = new JobPosting(
                    "https://example.com",
                    "Company",
                    "Title",
                    "Location",
                    "Description"
            );

            AnalysisResult analysisResult = new AnalysisResult();
            JobAnalysis mappedEntity = new JobAnalysis();

            String expectedSystemPrompt = PromptBuilder.buildSystemPrompt();
            String expectedUserPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(expectedSystemPrompt, expectedUserPrompt))
                    .thenReturn(analysisResult);
            when(analysisMapper.toEntity(jobPosting, analysisResult))
                    .thenReturn(mappedEntity);
            when(jobAnalysisRepository.save(mappedEntity))
                    .thenThrow(new RuntimeException("DB error"));

            AnalysisFailedException exception = assertThrows(
                    AnalysisFailedException.class,
                    () -> service.analyzeAndSave(jobPosting, request)
            );

            assertEquals("Analysis process failed: DB error", exception.getMessage());

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verify(analysisMapper).toEntity(jobPosting, analysisResult);
            verify(jobAnalysisRepository).save(mappedEntity);
            verifyNoMoreInteractions(openAiClient, analysisMapper, jobAnalysisRepository, jobPostingRepository);
        }

        @Test
        void shouldWrapDataAccessExceptionIntoAnalysisFailedException() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(
                    openAiClient,
                    jobAnalysisRepository,
                    jobPostingRepository,
                    analysisMapper
            );

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer");
            request.setJobPostingDescription("Backend role");

            JobPosting jobPosting = new JobPosting(
                    "https://example.com",
                    "Company",
                    "Title",
                    "Location",
                    "Description"
            );

            AnalysisResult analysisResult = new AnalysisResult();
            JobAnalysis mappedEntity = new JobAnalysis();

            String expectedSystemPrompt = PromptBuilder.buildSystemPrompt();
            String expectedUserPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(expectedSystemPrompt, expectedUserPrompt))
                    .thenReturn(analysisResult);
            when(analysisMapper.toEntity(jobPosting, analysisResult))
                    .thenReturn(mappedEntity);
            when(jobAnalysisRepository.save(mappedEntity))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException("DB error"));

            AnalysisFailedException exception = assertThrows(
                    AnalysisFailedException.class,
                    () -> service.analyzeAndSave(jobPosting, request)
            );

            assertEquals("Failed to save analysis results", exception.getMessage());

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verify(analysisMapper).toEntity(jobPosting, analysisResult);
            verify(jobAnalysisRepository).save(mappedEntity);
            verifyNoMoreInteractions(openAiClient, analysisMapper, jobAnalysisRepository, jobPostingRepository);
        }
    }
}
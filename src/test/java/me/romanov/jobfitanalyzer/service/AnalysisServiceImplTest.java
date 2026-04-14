package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.ai.OpenAiClient;
import me.romanov.jobfitanalyzer.ai.PromptBuilder;
import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.mapper.AnalysisMapper;
import me.romanov.jobfitanalyzer.repository.JobAnalysisRepository;
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
    private AnalysisMapper analysisMapper;

    @Nested
    class AnalyzeTests {

        @Test
        void shouldCallOpenAiWithBuiltPromptsAndReturnResult() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, jobAnalysisRepository, analysisMapper);

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
            verifyNoInteractions(jobAnalysisRepository, analysisMapper);
        }

        @Test
        void shouldPropagateExceptionWhenOpenAiFails() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, jobAnalysisRepository, analysisMapper);

            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile("Java developer");
            request.setJobPostingDescription("Backend role");

            String systemPrompt = PromptBuilder.buildSystemPrompt();
            String userPrompt = PromptBuilder.buildUserPrompt(
                    request.getCandidateProfile(),
                    request.getJobPostingDescription()
            );

            when(openAiClient.callOpenAi(systemPrompt, userPrompt))
                    .thenThrow(new RuntimeException("OpenAI error"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> service.analyze(request)
            );

            assertEquals("OpenAI error", exception.getMessage());

            verify(openAiClient).callOpenAi(systemPrompt, userPrompt);
            verifyNoInteractions(jobAnalysisRepository, analysisMapper);
        }
    }

    @Nested
    class AnalyzeAndSaveTests {

        @Test
        void shouldAnalyzeMapAndSaveEntity() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, jobAnalysisRepository, analysisMapper);

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

            JobAnalysis actualResult = service.analyzeAndSave(jobPosting, request);

            assertSame(mappedEntity, actualResult);

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verify(analysisMapper).toEntity(jobPosting, analysisResult);
            verify(jobAnalysisRepository).save(mappedEntity);
            verifyNoMoreInteractions(openAiClient, analysisMapper, jobAnalysisRepository);
        }

        @Test
        void shouldPropagateExceptionWhenOpenAiFails() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, jobAnalysisRepository, analysisMapper);

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
                    .thenThrow(new RuntimeException("OpenAI error"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> service.analyzeAndSave(jobPosting, request)
            );

            assertEquals("OpenAI error", exception.getMessage());

            verify(openAiClient).callOpenAi(systemPrompt, userPrompt);
            verifyNoInteractions(analysisMapper, jobAnalysisRepository);
        }

        @Test
        void shouldPropagateExceptionWhenSavingFails() {
            AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, jobAnalysisRepository, analysisMapper);

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

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> service.analyzeAndSave(jobPosting, request)
            );

            assertEquals("DB error", exception.getMessage());

            verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
            verify(analysisMapper).toEntity(jobPosting, analysisResult);
            verify(jobAnalysisRepository).save(mappedEntity);
        }
    }
}
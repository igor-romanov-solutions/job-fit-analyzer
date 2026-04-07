package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.ai.OpenAiClient;
import me.romanov.jobfitanalyzer.ai.PromptBuilder;
import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import me.romanov.jobfitanalyzer.mapper.AnalysisMapper;
import me.romanov.jobfitanalyzer.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private AnalysisMapper analysisMapper;

    @Test
    void analyzeAndSaveShouldCallOpenAiMapAndSaveEntity() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        AnalysisRequest request = new AnalysisRequest();
        request.setCandidateProfile("Java developer with Spring");
        request.setVacancyText("Senior Java Backend role");

        AnalysisResult expectedResult = new AnalysisResult();
        expectedResult.setRoleType("BACKEND");

        AnalysisEntity mappedEntity = new AnalysisEntity();

        String expectedSystemPrompt = PromptBuilder.buildSystemPrompt();
        String expectedUserPrompt = PromptBuilder.buildUserPrompt(
                request.getCandidateProfile(),
                request.getVacancyText()
        );

        when(openAiClient.callOpenAi(expectedSystemPrompt, expectedUserPrompt)).thenReturn(expectedResult);
        when(analysisMapper.toEntity(request, expectedResult)).thenReturn(mappedEntity);

        AnalysisResult actualResult = service.analyzeAndSave(request);

        assertSame(expectedResult, actualResult);

        verify(openAiClient).callOpenAi(expectedSystemPrompt, expectedUserPrompt);
        verify(analysisMapper).toEntity(request, expectedResult);
        verify(analysisRepository).save(mappedEntity);
        verifyNoMoreInteractions(openAiClient, analysisMapper, analysisRepository);
    }

    @Test
    void getHistoryShouldMapEntitiesToDtos() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        AnalysisEntity first = new AnalysisEntity();
        AnalysisEntity second = new AnalysisEntity();

        AnalysisHistoryItemDto firstDto = new AnalysisHistoryItemDto();
        AnalysisHistoryItemDto secondDto = new AnalysisHistoryItemDto();

        when(analysisRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(first, second));
        when(analysisMapper.toHistoryItemDto(first)).thenReturn(firstDto);
        when(analysisMapper.toHistoryItemDto(second)).thenReturn(secondDto);

        List<AnalysisHistoryItemDto> result = service.getHistory();

        assertEquals(2, result.size());
        assertEquals(firstDto, result.get(0));
        assertEquals(secondDto, result.get(1));

        verify(analysisRepository).findAllByOrderByCreatedAtDesc();
        verify(analysisMapper).toHistoryItemDto(first);
        verify(analysisMapper).toHistoryItemDto(second);
        verifyNoMoreInteractions(analysisRepository, analysisMapper);
    }

    @Test
    void getAnalysisShouldReturnMappedDtoWhenEntityExists() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        Long id = 42L;
        AnalysisEntity entity = new AnalysisEntity();
        AnalysisViewDto dto = new AnalysisViewDto();

        when(analysisRepository.findById(id)).thenReturn(Optional.of(entity));
        when(analysisMapper.toViewDto(entity)).thenReturn(dto);

        AnalysisViewDto result = service.getAnalysis(id);

        assertSame(dto, result);

        verify(analysisRepository).findById(id);
        verify(analysisMapper).toViewDto(entity);
        verifyNoMoreInteractions(analysisRepository, analysisMapper);
    }

    @Test
    void getAnalysisShouldThrowNotFoundWhenEntityMissing() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        Long id = 42L;
        when(analysisRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getAnalysis(id)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("Analysis not found: 42"));

        verify(analysisRepository).findById(id);
        verifyNoInteractions(analysisMapper);
    }

    @Test
    void analyzeAndSaveShouldPropagateExceptionWhenOpenAiFails() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        AnalysisRequest request = new AnalysisRequest();
        request.setCandidateProfile("Java developer");
        request.setVacancyText("Backend role");

        String systemPrompt = PromptBuilder.buildSystemPrompt();
        String userPrompt = PromptBuilder.buildUserPrompt(
                request.getCandidateProfile(),
                request.getVacancyText()
        );

        when(openAiClient.callOpenAi(systemPrompt, userPrompt))
                .thenThrow(new RuntimeException("OpenAI error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.analyzeAndSave(request)
        );

        assertEquals("OpenAI error", exception.getMessage());

        verify(openAiClient).callOpenAi(systemPrompt, userPrompt);
        verifyNoInteractions(analysisMapper, analysisRepository);
    }

    @Test
    void getHistoryShouldReturnEmptyListWhenNoAnalysesExist() {
        AnalysisServiceImpl service = new AnalysisServiceImpl(openAiClient, analysisRepository, analysisMapper);

        when(analysisRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        List<AnalysisHistoryItemDto> result = service.getHistory();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(analysisRepository).findAllByOrderByCreatedAtDesc();
        verifyNoInteractions(analysisMapper);
    }
}
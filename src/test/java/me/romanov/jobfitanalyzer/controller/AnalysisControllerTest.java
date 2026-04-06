package me.romanov.jobfitanalyzer.controller;

import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void getHistory_shouldReturnHistoryPage() throws Exception {
        AnalysisHistoryItemDto item = new AnalysisHistoryItemDto();
        when(analysisService.getHistory()).thenReturn(List.of(item));

        mockMvc.perform(get("/analyses"))
                .andExpect(status().isOk())
                .andExpect(view().name("analysis-history"))
                .andExpect(model().attributeExists("analyses"))
                .andExpect(model().attribute("analyses", List.of(item)));

        verify(analysisService).getHistory();
    }

    @Test
    void getAnalysis_shouldReturnDetailsPage() throws Exception {
        AnalysisViewDto analysis = new AnalysisViewDto();
        when(analysisService.getAnalysis(1L)).thenReturn(analysis);

        mockMvc.perform(get("/analyses/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("analysis-details"))
                .andExpect(model().attributeExists("analysis"))
                .andExpect(model().attribute("analysis", analysis));

        verify(analysisService).getAnalysis(1L);
    }

    @Test
    void getAnalysis_whenIdNotFound_shouldReturnError() throws Exception {
        when(analysisService.getAnalysis(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found"));

        mockMvc.perform(get("/analyses/999"))
                .andExpect(status().isNotFound());

        verify(analysisService).getAnalysis(999L);
    }
}
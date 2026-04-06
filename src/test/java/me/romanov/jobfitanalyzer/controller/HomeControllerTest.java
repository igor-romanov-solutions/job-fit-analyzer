package me.romanov.jobfitanalyzer.controller;

import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void showForm_shouldReturnIndexPageWithEmptyRequest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("analysisRequest"));

        verifyNoInteractions(analysisService);
    }

    @Test
    void analyze_shouldReturnIndexPageWithResult() throws Exception {
        AnalysisResult result = new AnalysisResult();
        when(analysisService.analyzeAndSave(org.mockito.ArgumentMatchers.any(AnalysisRequest.class)))
                .thenReturn(result);

        mockMvc.perform(post("/")
                        .param("candidateProfile", "Java developer")
                        .param("vacancyText", "Spring developer"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attributeExists("analysisRequest"));

        verify(analysisService).analyzeAndSave(org.mockito.ArgumentMatchers.any(AnalysisRequest.class));
    }

    @Test
    void analyze_whenServiceThrowsException_shouldReturnIndexPageWithError() throws Exception {
        when(analysisService.analyzeAndSave(org.mockito.ArgumentMatchers.any(AnalysisRequest.class)))
                .thenThrow(new RuntimeException("Boom"));

        mockMvc.perform(post("/")
                        .param("candidateProfile", "Java developer")
                        .param("vacancyText", "Spring developer"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeExists("analysisRequest"));

        verify(analysisService).analyzeAndSave(org.mockito.ArgumentMatchers.any(AnalysisRequest.class));
    }
}
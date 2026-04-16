package me.romanov.jobfitanalyzer.controller;

import me.romanov.jobfitanalyzer.domain.AnalysisFailedException;
import me.romanov.jobfitanalyzer.domain.ExternalServiceException;
import me.romanov.jobfitanalyzer.domain.JobPostingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new TestErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturn404ForJobPostingNotFound() throws Exception {
        mockMvc.perform(get("/test/job-posting-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errors/404"))
                .andExpect(model().attribute("message", "JobPosting not found: 1"))
                .andExpect(model().attribute("errorCode", "JOB_POSTING_NOT_FOUND"));
    }

    @Test
    void shouldReturn503ForExternalServiceException() throws Exception {
        mockMvc.perform(get("/test/external-service-error"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("errors/service-unavailable"))
                .andExpect(model().attribute("message",
                        "External service is temporarily unavailable. Please try again later."))
                .andExpect(model().attribute("errorCode", "EXTERNAL_SERVICE_ERROR"));
    }

    @Test
    void shouldReturn503ForAnalysisFailedException() throws Exception {
        mockMvc.perform(get("/test/analysis-failed"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("errors/service-unavailable"))
                .andExpect(model().attribute("message",
                        "External service is temporarily unavailable. Please try again later."))
                .andExpect(model().attribute("errorCode", "EXTERNAL_SERVICE_ERROR"));
    }

    @Test
    void shouldReturn500ForUnexpectedErrors() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("errors/error"))
                .andExpect(model().attribute("message", "Something went wrong. Please try again later."))
                .andExpect(model().attribute("errorCode", "INTERNAL_SERVER_ERROR"));
    }

    @RestController
    static class TestErrorController {

        @GetMapping("/test/job-posting-not-found")
        String jobPostingNotFound() {
            throw new JobPostingNotFoundException(1L);
        }

        @GetMapping("/test/external-service-error")
        String externalServiceError() {
            throw new ExternalServiceException("OpenAI error");
        }

        @GetMapping("/test/analysis-failed")
        String analysisFailed() {
            throw new AnalysisFailedException("Analysis failed");
        }

        @GetMapping("/test/unexpected-error")
        String unexpectedError() {
            throw new IllegalStateException("Unexpected");
        }
    }
}
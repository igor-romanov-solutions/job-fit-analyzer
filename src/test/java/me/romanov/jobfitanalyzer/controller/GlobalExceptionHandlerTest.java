package me.romanov.jobfitanalyzer.controller;

import me.romanov.jobfitanalyzer.domain.AnalysisFailedException;
import me.romanov.jobfitanalyzer.domain.ExternalServiceException;
import me.romanov.jobfitanalyzer.domain.JobPostingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void shouldAddErrorsWhenHandlingBindException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.reject("invalid", "Invalid value");

        BindException ex = new BindException(bindingResult);

        ModelAndView mav = handler.handleValidationErrors(ex);

        assertEquals("errors/bad-request", mav.getViewName());
        assertEquals(HttpStatus.BAD_REQUEST, mav.getStatus());
        assertEquals("Validation failed. Please check the submitted data.", mav.getModel().get("message"));
        assertEquals("VALIDATION_ERROR", mav.getModel().get("errorCode"));
        assertTrue(mav.getModel().containsKey("errors"));
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
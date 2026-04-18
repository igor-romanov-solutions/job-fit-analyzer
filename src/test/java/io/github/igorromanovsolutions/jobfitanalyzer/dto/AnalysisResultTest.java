package io.github.igorromanovsolutions.jobfitanalyzer.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnalysisResultTest {

    @Test
    void shouldBuildFallbackResult() {
        AnalysisResult result = AnalysisResult.fallback();

        assertNotNull(result);
        assertEquals("Unknown", result.getRoleType());
        assertEquals("Unknown", result.getJavaRelevance());
        assertEquals(List.of(), result.getPrimaryStack());
        assertEquals(List.of(), result.getSecondaryStack());
        assertEquals(List.of(), result.getNiceToHaveStack());
        assertEquals("Unknown", result.getSeniorityLevel());
        assertEquals("Unknown", result.getDomain());
        assertEquals("Unknown", result.getVacancyLanguage());
        assertEquals(List.of("Failed to analyze vacancy. Please try again."), result.getGaps());
    }
}
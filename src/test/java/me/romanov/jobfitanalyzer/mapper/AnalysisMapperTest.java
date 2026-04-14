package me.romanov.jobfitanalyzer.mapper;

import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisMapperTest {

    private final AnalysisMapper mapper = new AnalysisMapper();


    @Test
    void shouldMapAllFieldsToEntity() {
        AnalysisResult result = buildAnalysisResult();
        JobPosting jobPosting = new JobPosting("https://example.com", "Company", "Title", "Location", "Description");

        LocalDateTime before = LocalDateTime.now();
        JobAnalysis entity = mapper.toEntity(jobPosting, result);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(entity);

        assertSame(jobPosting, entity.getJobPosting());

        assertNotNull(entity.getCreatedAt());
        assertFalse(entity.getCreatedAt().isBefore(before));
        assertFalse(entity.getCreatedAt().isAfter(after));

        assertEquals(result.getRoleType(), entity.getRoleType());
        assertEquals(result.getJavaRelevance(), entity.getJavaRelevance());
        assertEquals(result.getSeniorityLevel(), entity.getSeniorityLevel());
        assertEquals(result.getDomain(), entity.getDomain());
        assertEquals(result.getVacancyLanguage(), entity.getVacancyLanguage());
        assertEquals(result.getRequiredGermanLevel(), entity.getRequiredGermanLevel());

        assertEquals(MappingUtils.join(result.getPrimaryStack()), entity.getPrimaryStack());
        assertEquals(MappingUtils.join(result.getSecondaryStack()), entity.getSecondaryStack());
        assertEquals(MappingUtils.join(result.getNiceToHaveStack()), entity.getNiceToHaveStack());
        assertEquals(MappingUtils.join(result.getGaps()), entity.getGaps());
    }

    private static @NonNull AnalysisResult buildAnalysisResult() {
        AnalysisResult result = new AnalysisResult();
        result.setRoleType("BACKEND");
        result.setJavaRelevance("HIGH");
        result.setSeniorityLevel("SENIOR");
        result.setDomain("FINTECH");
        result.setVacancyLanguage("ENGLISH");
        result.setRequiredGermanLevel("B2");
        result.setPrimaryStack(List.of("Java", "Spring"));
        result.setSecondaryStack(List.of("Kafka", "PostgreSQL"));
        result.setNiceToHaveStack(List.of("Docker", "Kubernetes"));
        result.setGaps(List.of("German language"));
        return result;
    }

    @Test
    void shouldMapFallbackResultCorrectly() {
        AnalysisResult fallback = AnalysisResult.fallback();
        JobPosting jobPosting = new JobPosting("https://example.com", "Company", "Title", "Location", "Description");

        JobAnalysis entity = mapper.toEntity(jobPosting, fallback);

        assertNotNull(entity);
        assertSame(jobPosting, entity.getJobPosting());
        assertNotNull(entity.getCreatedAt());

        assertEquals("Unknown", entity.getRoleType());
        assertEquals("Unknown", entity.getJavaRelevance());
        assertEquals("Unknown", entity.getSeniorityLevel());
        assertEquals("Unknown", entity.getDomain());
        assertEquals("Unknown", entity.getVacancyLanguage());

        assertEquals(MappingUtils.join(List.of()), entity.getPrimaryStack());
        assertEquals(MappingUtils.join(List.of()), entity.getSecondaryStack());
        assertEquals(MappingUtils.join(List.of()), entity.getNiceToHaveStack());

        assertEquals(
                MappingUtils.join(List.of("Failed to analyze vacancy. Please try again.")),
                entity.getGaps()
        );
    }
}
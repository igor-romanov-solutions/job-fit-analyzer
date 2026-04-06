package me.romanov.jobfitanalyzer.mapper;

import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisMapperTest {

    private final AnalysisMapper mapper = new AnalysisMapper();

    @Nested
    class ToEntityTests {
        @Test
        void shouldMapAllFieldsToEntity() {
            AnalysisRequest request = buildAnalysisRequest();
            AnalysisResult result = buildAnalysisResult();

            LocalDateTime before = LocalDateTime.now();
            AnalysisEntity entity = mapper.toEntity(request, result);
            LocalDateTime after = LocalDateTime.now();

            assertNotNull(entity);

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

            assertEquals(
                    MappingUtils.buildPreview(request.getVacancyText()),
                    entity.getJobDescriptionPreview()
            );
        }

        private static @NonNull AnalysisRequest buildAnalysisRequest() {
            AnalysisRequest request = new AnalysisRequest();
            request.setVacancyText("Senior Java Developer role with Spring, Kafka and PostgreSQL");
            return request;
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
            AnalysisRequest request = new AnalysisRequest();
            request.setVacancyText("Some vacancy text");

            AnalysisResult fallback = AnalysisResult.fallback();

            AnalysisEntity entity = mapper.toEntity(request, fallback);

            assertNotNull(entity);
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

            assertEquals(
                    MappingUtils.buildPreview(request.getVacancyText()),
                    entity.getJobDescriptionPreview()
            );
        }

        @Test
        void shouldBuildPreviewFromLongVacancyText() {

            AnalysisRequest request = new AnalysisRequest();
            String longText = "Java ".repeat(500); // long text
            request.setVacancyText(longText);

            AnalysisResult result = AnalysisResult.fallback(); // можно reuse

            AnalysisEntity entity = mapper.toEntity(request, result);

            assertNotNull(entity);

            String expectedPreview = MappingUtils.buildPreview(longText);

            assertEquals(expectedPreview, entity.getJobDescriptionPreview());
            assertNotEquals(longText, entity.getJobDescriptionPreview());
        }
    }

    @Nested
    class ToHistoryItemDtoTests {
        @Test
        void shouldMapEntityToHistoryItemDto() {
            AnalysisEntity entity = buildAnalysisEntity();

            AnalysisHistoryItemDto dto = mapper.toHistoryItemDto(entity);

            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getRoleType(), dto.getRoleType());
            assertEquals(entity.getJobDescriptionPreview(), dto.getJobDescriptionPreview());
        }
    }

    private static @NonNull AnalysisEntity buildAnalysisEntity() {
        AnalysisEntity entity = new AnalysisEntity();
        entity.setId(1L);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRoleType("BACKEND");
        entity.setSeniorityLevel("SENIOR");
        entity.setDomain("FINTECH");
        entity.setVacancyLanguage("EN");
        entity.setJavaRelevance("HIGH");
        entity.setJobDescriptionPreview("preview");
        return entity;
    }

    @Nested
    class ToViewDtoTests {
        @Test
        void shouldMapEntityToViewDto() {
            AnalysisEntity entity = buildAnalysisEntity();

            AnalysisViewDto dto = mapper.toViewDto(entity);

            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getRoleType(), dto.getRoleType());

            assertEquals(
                    MappingUtils.split(entity.getPrimaryStack()),
                    dto.getPrimaryStack()
            );
            assertEquals(
                    MappingUtils.split(entity.getSecondaryStack()),
                    dto.getSecondaryStack()
            );
        }
    }

}
package me.romanov.jobfitanalyzer.mapper;

import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class AnalysisMapper {

    public AnalysisEntity toEntity(AnalysisRequest request, AnalysisResult result) {
        AnalysisEntity entity = new AnalysisEntity();
        entity.setCreatedAt(LocalDateTime.now());
        entity.setRoleType(result.getRoleType());
        entity.setJavaRelevance(result.getJavaRelevance());
        entity.setSeniorityLevel(result.getSeniorityLevel());
        entity.setDomain(result.getDomain());
        entity.setVacancyLanguage(result.getVacancyLanguage());
        entity.setRequiredGermanLevel(result.getRequiredGermanLevel());
        entity.setPrimaryStack(join(result.getPrimaryStack()));
        entity.setSecondaryStack(join(result.getSecondaryStack()));
        entity.setNiceToHaveStack(join(result.getNiceToHaveStack()));
        entity.setGaps(join(result.getGaps()));
        entity.setJobDescriptionPreview(buildPreview(request.getVacancyText()));
        return entity;
    }

    private String join(List<String> list) {
        return list == null ? null : String.join(", ", list);
    }

    private String buildPreview(String text) {
        if (text == null) return null;
        int max = 300;
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    public AnalysisHistoryItemDto toHistoryItemDto(AnalysisEntity entity) {
        return AnalysisHistoryItemDto.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .roleType(entity.getRoleType())
                .seniorityLevel(entity.getSeniorityLevel())
                .domain(entity.getDomain())
                .vacancyLanguage(entity.getVacancyLanguage())
                .javaRelevance(entity.getJavaRelevance())
                .jobDescriptionPreview(entity.getJobDescriptionPreview())
                .build();
    }

    public AnalysisViewDto toViewDto(AnalysisEntity entity) {
        return AnalysisViewDto.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .roleType(entity.getRoleType())
                .javaRelevance(entity.getJavaRelevance())
                .seniorityLevel(entity.getSeniorityLevel())
                .domain(entity.getDomain())
                .vacancyLanguage(entity.getVacancyLanguage())
                .requiredGermanLevel(entity.getRequiredGermanLevel())
                .primaryStack(split(entity.getPrimaryStack()))
                .secondaryStack(split(entity.getSecondaryStack()))
                .niceToHaveStack(split(entity.getNiceToHaveStack()))
                .gaps(split(entity.getGaps()))
                .jobDescriptionPreview(entity.getJobDescriptionPreview())
                .build();
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .toList();
    }
}

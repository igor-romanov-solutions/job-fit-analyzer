package me.romanov.jobfitanalyzer.mapper;

import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
        entity.setPrimaryStack(MappingUtils.join(result.getPrimaryStack()));
        entity.setSecondaryStack(MappingUtils.join(result.getSecondaryStack()));
        entity.setNiceToHaveStack(MappingUtils.join(result.getNiceToHaveStack()));
        entity.setGaps(MappingUtils.join(result.getGaps()));
        entity.setJobDescriptionPreview(MappingUtils.buildPreview(request.getVacancyText()));
        return entity;
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
                .primaryStack(MappingUtils.split(entity.getPrimaryStack()))
                .secondaryStack(MappingUtils.split(entity.getSecondaryStack()))
                .niceToHaveStack(MappingUtils.split(entity.getNiceToHaveStack()))
                .gaps(MappingUtils.split(entity.getGaps()))
                .jobDescriptionPreview(entity.getJobDescriptionPreview())
                .build();
    }
}

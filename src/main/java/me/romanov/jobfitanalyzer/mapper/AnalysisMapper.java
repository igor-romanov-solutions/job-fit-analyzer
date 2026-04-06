package me.romanov.jobfitanalyzer.mapper;

import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
}

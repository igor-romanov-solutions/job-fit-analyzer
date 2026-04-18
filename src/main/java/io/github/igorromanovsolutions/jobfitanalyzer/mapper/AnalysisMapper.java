package io.github.igorromanovsolutions.jobfitanalyzer.mapper;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobAnalysis;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPosting;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AnalysisMapper {

    public JobAnalysis toEntity(JobPosting jobPosting, AnalysisResult result) {
        JobAnalysis entity = new JobAnalysis();
        entity.setJobPosting(jobPosting);
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
        return entity;
    }
}

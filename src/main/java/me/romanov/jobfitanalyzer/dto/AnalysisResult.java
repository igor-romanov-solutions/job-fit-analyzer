package me.romanov.jobfitanalyzer.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisResult {

    public static final String UNKNOWN = "Unknown";

    private String roleType;
    private String javaRelevance;
    private List<String> primaryStack;
    private List<String> secondaryStack;
    private List<String> niceToHaveStack;
    private String seniorityLevel;
    private String domain;
    private String vacancyLanguage;
    private String requiredGermanLevel;
    private List<String> gaps;

    public static AnalysisResult fallback() {
        AnalysisResult analysisResult = new AnalysisResult();
        analysisResult.setRoleType(UNKNOWN);
        analysisResult.setJavaRelevance(UNKNOWN);
        analysisResult.setPrimaryStack(List.of());
        analysisResult.setSecondaryStack(List.of());
        analysisResult.setNiceToHaveStack(List.of());
        analysisResult.setSeniorityLevel(UNKNOWN);
        analysisResult.setDomain(UNKNOWN);
        analysisResult.setVacancyLanguage(UNKNOWN);
        analysisResult.setGaps(List.of("Failed to analyze vacancy. Please try again."));
        return analysisResult;
    }
}

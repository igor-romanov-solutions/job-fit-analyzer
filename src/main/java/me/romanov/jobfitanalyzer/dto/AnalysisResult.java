package me.romanov.jobfitanalyzer.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisResult {
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
        analysisResult.setRoleType("Unknown");
        analysisResult.setJavaRelevance("Unknown");
        analysisResult.setPrimaryStack(List.of());
        analysisResult.setSecondaryStack(List.of());
        analysisResult.setNiceToHaveStack(List.of());
        analysisResult.setSeniorityLevel("Unknown");
        analysisResult.setDomain("Unknown");
        analysisResult.setVacancyLanguage("Unknown");
        analysisResult.setGaps(List.of("Failed to analyze vacancy. Please try again."));
        return analysisResult;
    }
}

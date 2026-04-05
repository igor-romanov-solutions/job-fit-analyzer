package me.romanov.jobfitanalyzer.dto;

import lombok.Data;

@Data
public class AnalysisRequest {
    private String candidateProfile;
    private String vacancyText;
}

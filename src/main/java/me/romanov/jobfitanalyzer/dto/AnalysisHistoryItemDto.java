package me.romanov.jobfitanalyzer.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisHistoryItemDto {

    private Long id;
    private LocalDateTime createdAt;
    private String roleType;
    private String seniorityLevel;
    private String domain;
    private String vacancyLanguage;
    private String javaRelevance;
    private String jobDescriptionPreview;
}

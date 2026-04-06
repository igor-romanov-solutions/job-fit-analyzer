package me.romanov.jobfitanalyzer.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisViewDto {

    private Long id;
    private LocalDateTime createdAt;

    private String roleType;
    private String javaRelevance;
    private String seniorityLevel;
    private String domain;
    private String vacancyLanguage;
    private String requiredGermanLevel;

    private List<String> primaryStack;
    private List<String> secondaryStack;
    private List<String> niceToHaveStack;
    private List<String> gaps;

    private String jobDescriptionPreview;
}

package me.romanov.jobfitanalyzer.dto;

import lombok.Data;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
public class JobPostingFilterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private JobPostingStatus status;
    private String javaRelevance;
    private String requiredGermanLevel;
}
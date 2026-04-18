package io.github.igorromanovsolutions.jobfitanalyzer.dto;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPostingStatus;
import lombok.Data;

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
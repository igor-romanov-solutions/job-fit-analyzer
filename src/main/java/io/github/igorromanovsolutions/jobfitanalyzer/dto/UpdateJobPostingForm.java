package io.github.igorromanovsolutions.jobfitanalyzer.dto;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPostingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateJobPostingForm(
        String sourceUrl,
        String companyName,
        String jobTitle,
        String location,
        @NotBlank String description,
        @NotNull JobPostingStatus status
) {
}

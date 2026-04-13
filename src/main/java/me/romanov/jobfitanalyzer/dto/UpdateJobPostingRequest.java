package me.romanov.jobfitanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;

public record UpdateJobPostingRequest(
        String sourceUrl,
        String companyName,
        String jobTitle,
        String location,
        @NotBlank String description,
        @NotNull JobPostingStatus status
) {
}

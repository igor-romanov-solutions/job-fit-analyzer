package me.romanov.jobfitanalyzer.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateJobPostingRequest(
        String sourceUrl,
        String companyName,
        String jobTitle,
        String location,
        @NotBlank String description
) {
}
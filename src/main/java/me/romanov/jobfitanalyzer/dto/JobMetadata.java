package me.romanov.jobfitanalyzer.dto;

public record JobMetadata(
        String companyName,
        String jobTitle,
        String location,
        String description
) {
}

package io.github.igorromanovsolutions.jobfitanalyzer.dto;

public record JobMetadata(
        String companyName,
        String jobTitle,
        String location,
        String description
) {
}

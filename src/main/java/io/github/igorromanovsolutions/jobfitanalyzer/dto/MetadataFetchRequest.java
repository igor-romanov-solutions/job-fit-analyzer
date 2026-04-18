package io.github.igorromanovsolutions.jobfitanalyzer.dto;

import jakarta.validation.constraints.NotBlank;

public record MetadataFetchRequest(
        @NotBlank String sourceUrl
) {
}

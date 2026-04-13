package me.romanov.jobfitanalyzer.dto;

import jakarta.validation.constraints.NotBlank;

public record MetadataFetchRequest(
        @NotBlank String sourceUrl
) {
}

package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobMetadata;

import java.util.Optional;

public interface JobMetadataFetcher {

    Optional<JobMetadata> fetch(String sourceUrl);
}

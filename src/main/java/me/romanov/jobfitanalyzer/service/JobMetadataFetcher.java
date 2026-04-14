package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.dto.JobMetadata;

import java.util.Optional;

public interface JobMetadataFetcher {

    Optional<JobMetadata> fetch(String sourceUrl);
}

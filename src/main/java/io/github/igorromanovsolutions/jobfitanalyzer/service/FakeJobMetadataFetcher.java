package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobMetadata;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FakeJobMetadataFetcher implements JobMetadataFetcher {

    @Override
    public Optional<JobMetadata> fetch(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new JobMetadata(
                "Sample Company",
                "Sample Java Developer",
                "Zurich",
                "Sample job description fetched from URL."
        ));
    }
}

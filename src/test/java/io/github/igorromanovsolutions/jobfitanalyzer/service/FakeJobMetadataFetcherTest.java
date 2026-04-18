package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FakeJobMetadataFetcherTest {

    private final FakeJobMetadataFetcher fetcher = new FakeJobMetadataFetcher();

    @Nested
    class FetchTests {

        @Test
        void shouldReturnSampleMetadataForValidUrl() {
            Optional<JobMetadata> result = fetcher.fetch("https://example.com/job/123");

            assertTrue(result.isPresent());

            JobMetadata metadata = result.get();
            assertEquals("Sample Company", metadata.companyName());
            assertEquals("Sample Java Developer", metadata.jobTitle());
            assertEquals("Zurich", metadata.location());
            assertEquals("Sample job description fetched from URL.", metadata.description());
        }

        @Test
        void shouldReturnEmptyForNullUrl() {
            Optional<JobMetadata> result = fetcher.fetch(null);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnEmptyForBlankUrl() {
            Optional<JobMetadata> result = fetcher.fetch("   ");

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnMetadataForTrimmedUrlToo() {
            Optional<JobMetadata> result = fetcher.fetch("  https://example.com/job/123  ");

            assertTrue(result.isPresent());

            JobMetadata metadata = result.get();
            assertEquals("Sample Company", metadata.companyName());
            assertEquals("Sample Java Developer", metadata.jobTitle());
            assertEquals("Zurich", metadata.location());
            assertEquals("Sample job description fetched from URL.", metadata.description());
        }
    }
}
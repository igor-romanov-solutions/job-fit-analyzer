package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.dto.JobMetadata;
import me.romanov.jobfitanalyzer.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JobPostingServiceTest {

    private JobPostingRepository jobPostingRepository;
    private JobPostingService jobPostingService;
    private JobMetadataFetcher jobMetadataFetcher;

    @BeforeEach
    void setUp() {
        jobPostingRepository = mock(JobPostingRepository.class);
        jobMetadataFetcher = mock(JobMetadataFetcher.class);
        jobPostingService = new JobPostingService(jobPostingRepository, jobMetadataFetcher);
    }

    @Test
    void shouldCreateJobPostingWithNewStatus() {
        CreateJobPostingRequest request = new CreateJobPostingRequest(
                " https://www.linkedin.com/jobs/view/123 ",
                " UBS ",
                " Senior Java Developer ",
                " Basel ",
                " Strong Java and Spring Boot experience required "
        );

        when(jobPostingRepository.save(any(JobPosting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobPosting created = jobPostingService.create(request);

        ArgumentCaptor<JobPosting> captor = ArgumentCaptor.forClass(JobPosting.class);
        verify(jobPostingRepository).save(captor.capture());

        JobPosting saved = captor.getValue();

        assertThat(saved.getSourceUrl()).isEqualTo("https://www.linkedin.com/jobs/view/123");
        assertThat(saved.getCompanyName()).isEqualTo("UBS");
        assertThat(saved.getJobTitle()).isEqualTo("Senior Java Developer");
        assertThat(saved.getDescription()).isEqualTo("Strong Java and Spring Boot experience required");
        assertThat(saved.getStatus()).isEqualTo(JobPostingStatus.NEW);

        assertThat(created.getStatus()).isEqualTo(JobPostingStatus.NEW);
    }

    @Test
    void shouldFetchMetadata_whenSourceUrlIsValid() {
        // given
        String sourceUrl = "https://example.com/job/123";

        JobMetadata metadata = new JobMetadata(
                "UBS",
                "Senior Java Developer",
                "Zurich",
                "Job description fetched from URL."
        );

        when(jobMetadataFetcher.fetch("https://example.com/job/123"))
                .thenReturn(Optional.of(metadata));

        // when
        Optional<JobMetadata> result = jobPostingService.fetchMetadata(sourceUrl);

        // then
        assertThat(result)
                .isPresent()
                .contains(metadata);

        verify(jobMetadataFetcher).fetch("https://example.com/job/123");
    }

    @Test
    void shouldReturnEmptyIfUrlIsBlank() {
        Optional<JobMetadata> result = jobPostingService.fetchMetadata("   ");

        assertThat(result).isEmpty();
        verifyNoInteractions(jobMetadataFetcher);
    }

    @Test
    void shouldReturnEmptyIfFetcherReturnsEmpty() {
        when(jobMetadataFetcher.fetch("https://example.com"))
                .thenReturn(Optional.empty());

        Optional<JobMetadata> result = jobPostingService.fetchMetadata("https://example.com");

        assertThat(result).isEmpty();
        verify(jobMetadataFetcher).fetch("https://example.com");
    }
}
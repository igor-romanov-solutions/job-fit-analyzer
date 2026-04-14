package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingNotFoundException;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.dto.JobMetadata;
import me.romanov.jobfitanalyzer.dto.JobPostingFilterRequest;
import me.romanov.jobfitanalyzer.dto.UpdateJobPostingRequest;
import me.romanov.jobfitanalyzer.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobPostingServiceImplTest {

    private JobPostingRepository jobPostingRepository;
    private JobPostingServiceImpl jobPostingService;
    private JobMetadataFetcher jobMetadataFetcher;

    @BeforeEach
    void setUp() {
        jobPostingRepository = mock(JobPostingRepository.class);
        jobMetadataFetcher = mock(JobMetadataFetcher.class);
        jobPostingService = new JobPostingServiceImpl(jobPostingRepository, jobMetadataFetcher);
    }

    @Nested
    class CreateTests {
        @Test
        void shouldCreateJobPostingWithNormalizedFieldsAndNewStatus() {
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

            assertEquals("https://www.linkedin.com/jobs/view/123", saved.getSourceUrl());
            assertEquals("UBS", saved.getCompanyName());
            assertEquals("Senior Java Developer", saved.getJobTitle());
            assertEquals("Basel", saved.getLocation());
            assertEquals("Strong Java and Spring Boot experience required", saved.getDescription());
            assertEquals(JobPostingStatus.NEW, saved.getStatus());

            assertSame(saved, created);
        }

        @Test
        void shouldThrowWhenCreateRequestIsNull() {
            assertThrows(NullPointerException.class, () -> jobPostingService.create(null));
        }

        @Test
        void shouldConvertBlankFieldsToNullWhenCreatingJobPosting() {
            CreateJobPostingRequest request = new CreateJobPostingRequest(
                    "   ",
                    "   ",
                    "   ",
                    "   ",
                    " Some description "
            );

            when(jobPostingRepository.save(any(JobPosting.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            jobPostingService.create(request);

            ArgumentCaptor<JobPosting> captor = ArgumentCaptor.forClass(JobPosting.class);
            verify(jobPostingRepository).save(captor.capture());

            JobPosting saved = captor.getValue();

            assertNull(saved.getSourceUrl());
            assertNull(saved.getCompanyName());
            assertNull(saved.getJobTitle());
            assertNull(saved.getLocation());
            assertEquals("Some description", saved.getDescription());
            assertEquals(JobPostingStatus.NEW, saved.getStatus());
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateExistingJobPostingWithNormalizedFields() {
            Long id = 1L;

            JobPosting existing = new JobPosting(
                    "https://old.example.com",
                    "Old Company",
                    "Old Title",
                    "Old Location",
                    "Old description"
            );
            existing.setId(id);
            existing.setStatus(JobPostingStatus.NEW);

            UpdateJobPostingRequest request = new UpdateJobPostingRequest(
                    " https://new.example.com ",
                    " New Company ",
                    " New Title ",
                    " New Location ",
                    " Updated description ",
                    JobPostingStatus.ANALYZED
            );

            when(jobPostingRepository.findById(id)).thenReturn(Optional.of(existing));

            JobPosting updated = jobPostingService.update(id, request);

            assertSame(existing, updated);
            assertEquals("https://new.example.com", updated.getSourceUrl());
            assertEquals("New Company", updated.getCompanyName());
            assertEquals("New Title", updated.getJobTitle());
            assertEquals("New Location", updated.getLocation());
            assertEquals("Updated description", updated.getDescription());
            assertEquals(JobPostingStatus.ANALYZED, updated.getStatus());

            verify(jobPostingRepository).findById(id);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldThrowWhenUpdateRequestIsNull() {
            assertThrows(NullPointerException.class, () -> jobPostingService.update(1L, null));

            verifyNoInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldThrowWhenJobPostingNotFound() {
            Long id = 1L;
            UpdateJobPostingRequest request = new UpdateJobPostingRequest(
                    "https://new.example.com",
                    "New Company",
                    "New Title",
                    "New Location",
                    "Updated description",
                    JobPostingStatus.ANALYZED
            );

            when(jobPostingRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(JobPostingNotFoundException.class, () -> jobPostingService.update(id, request));

            verify(jobPostingRepository).findById(id);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldConvertBlankFieldsToNullWhenUpdatingJobPosting() {
            Long id = 1L;

            JobPosting existing = new JobPosting(
                    "https://old.example.com",
                    "Old Company",
                    "Old Title",
                    "Old Location",
                    "Old description"
            );
            existing.setId(id);
            existing.setStatus(JobPostingStatus.NEW);

            UpdateJobPostingRequest request = new UpdateJobPostingRequest(
                    "   ",
                    "   ",
                    "   ",
                    "   ",
                    " Updated description ",
                    JobPostingStatus.PROCESSED
            );

            when(jobPostingRepository.findById(id)).thenReturn(Optional.of(existing));

            jobPostingService.update(id, request);

            assertNull(existing.getSourceUrl());
            assertNull(existing.getCompanyName());
            assertNull(existing.getJobTitle());
            assertNull(existing.getLocation());
            assertEquals("Updated description", existing.getDescription());
            assertEquals(JobPostingStatus.PROCESSED, existing.getStatus());

            verify(jobPostingRepository).findById(id);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

    @Nested
    class FetchMetadataTests {

        @Test
        void shouldFetchMetadataWhenSourceUrlIsValid() {
            String sourceUrl = "https://example.com/job/123";

            JobMetadata metadata = new JobMetadata(
                    "UBS",
                    "Senior Java Developer",
                    "Zurich",
                    "Job description fetched from URL."
            );

            when(jobMetadataFetcher.fetch(sourceUrl)).thenReturn(Optional.of(metadata));

            Optional<JobMetadata> result = jobPostingService.fetchMetadata(sourceUrl);

            assertThat(result)
                    .isPresent()
                    .contains(metadata);

            verify(jobMetadataFetcher).fetch(sourceUrl);
            verifyNoMoreInteractions(jobMetadataFetcher);
            verifyNoInteractions(jobPostingRepository);
        }

        @Test
        void shouldReturnEmptyIfUrlIsBlank() {
            Optional<JobMetadata> result = jobPostingService.fetchMetadata("   ");

            assertThat(result).isEmpty();

            verifyNoInteractions(jobMetadataFetcher);
            verifyNoInteractions(jobPostingRepository);
        }

        @Test
        void shouldReturnEmptyIfUrlIsNull() {
            Optional<JobMetadata> result = jobPostingService.fetchMetadata(null);

            assertThat(result).isEmpty();

            verifyNoInteractions(jobMetadataFetcher);
            verifyNoInteractions(jobPostingRepository);
        }

        @Test
        void shouldReturnEmptyIfFetcherReturnsEmpty() {
            String sourceUrl = "https://example.com";

            when(jobMetadataFetcher.fetch(sourceUrl)).thenReturn(Optional.empty());

            Optional<JobMetadata> result = jobPostingService.fetchMetadata(sourceUrl);

            assertThat(result).isEmpty();

            verify(jobMetadataFetcher).fetch(sourceUrl);
            verifyNoMoreInteractions(jobMetadataFetcher);
            verifyNoInteractions(jobPostingRepository);
        }

        @Test
        void shouldTrimSourceUrlBeforeFetchingMetadata() {
            String sourceUrl = " https://example.com/job/123 ";

            when(jobMetadataFetcher.fetch("https://example.com/job/123"))
                    .thenReturn(Optional.empty());

            Optional<JobMetadata> result = jobPostingService.fetchMetadata(sourceUrl);

            assertThat(result).isEmpty();

            verify(jobMetadataFetcher).fetch("https://example.com/job/123");
            verifyNoMoreInteractions(jobMetadataFetcher);
            verifyNoInteractions(jobPostingRepository);
        }
    }

    @Nested
    class FindAllTests {

        @Test
        void shouldReturnAllJobsOrderedByCreatedAtDesc() {
            JobPosting first = new JobPosting("https://a.com", "A", "A", "A", "A");
            JobPosting second = new JobPosting("https://b.com", "B", "B", "B", "B");

            when(jobPostingRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(second, first));

            List<JobPosting> result = jobPostingService.findAll();

            assertThat(result).containsExactly(second, first);

            verify(jobPostingRepository).findAllByOrderByCreatedAtDesc();
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldReturnEmptyListWhenNoJobsExist() {
            when(jobPostingRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            List<JobPosting> result = jobPostingService.findAll();

            assertThat(result).isEmpty();

            verify(jobPostingRepository).findAllByOrderByCreatedAtDesc();
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

    @Nested
    class FindByStatusTests {

        @Test
        void shouldReturnJobsByStatusOrderedByCreatedAtDesc() {
            JobPosting first = new JobPosting("https://a.com", "A", "A", "A", "A");
            JobPosting second = new JobPosting("https://b.com", "B", "B", "B", "B");

            when(jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobPostingStatus.NEW))
                    .thenReturn(List.of(second, first));

            List<JobPosting> result = jobPostingService.findByStatus(JobPostingStatus.NEW);

            assertThat(result).containsExactly(second, first);

            verify(jobPostingRepository).findByStatusOrderByCreatedAtDesc(JobPostingStatus.NEW);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldReturnEmptyListWhenNoJobsForStatusExist() {
            when(jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobPostingStatus.REJECTED))
                    .thenReturn(List.of());

            List<JobPosting> result = jobPostingService.findByStatus(JobPostingStatus.REJECTED);

            assertThat(result).isEmpty();

            verify(jobPostingRepository).findByStatusOrderByCreatedAtDesc(JobPostingStatus.REJECTED);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

    @Nested
    class FindByIdTests {

        @Test
        void shouldReturnJobWhenFound() {
            Long id = 1L;
            JobPosting jobPosting = new JobPosting("https://example.com", "Company", "Title", "Location", "Description");

            when(jobPostingRepository.findById(id)).thenReturn(Optional.of(jobPosting));

            JobPosting result = jobPostingService.findById(id);

            assertSame(jobPosting, result);

            verify(jobPostingRepository).findById(id);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldThrowWhenJobNotFound() {
            Long id = 1L;

            when(jobPostingRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(JobPostingNotFoundException.class, () -> jobPostingService.findById(id));

            verify(jobPostingRepository).findById(id);
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

    @Nested
    class FindByFilterTests {

        @Test
        void shouldReturnJobsByStatusAndJavaRelevance() {
            JobPosting job = new JobPosting("https://example.com", "Company", "Title", "Zurich", "Description");
            job.setStatus(JobPostingStatus.NEW);

            JobAnalysis analysis = new JobAnalysis();
            analysis.setJobPosting(job);
            analysis.setJavaRelevance("LOW");
            job.getAnalyses().add(analysis);

            when(jobPostingRepository.findAll(ArgumentMatchers.<Specification<JobPosting>>any()))
                    .thenReturn(List.of(job));

            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setStatus(JobPostingStatus.NEW);
            filter.setJavaRelevance("LOW");

            List<JobPosting> result = jobPostingService.findByFilter(filter);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isSameAs(job);

            verify(jobPostingRepository).findAll(ArgumentMatchers.<Specification<JobPosting>>any());
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

    @Nested
    class UpdateStatusByFilterTests {

        @Test
        void shouldUpdateStatusForFilteredJobsBasedOnLatestAnalysis() {
            JobPosting job = new JobPosting("https://a.com", "A", "A", "A", "A");
            job.setId(1L);
            job.setStatus(JobPostingStatus.NEW);

            JobAnalysis olderAnalysis = new JobAnalysis();
            olderAnalysis.setJobPosting(job);
            olderAnalysis.setJavaRelevance("HIGH");
            olderAnalysis.setRequiredGermanLevel("C1");
            olderAnalysis.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

            JobAnalysis latestAnalysis = new JobAnalysis();
            latestAnalysis.setJobPosting(job);
            latestAnalysis.setJavaRelevance("LOW");
            latestAnalysis.setRequiredGermanLevel("B2");
            latestAnalysis.setCreatedAt(java.time.LocalDateTime.now());

            job.getAnalyses().add(olderAnalysis);
            job.getAnalyses().add(latestAnalysis);

            when(jobPostingRepository.findAll(ArgumentMatchers.<Specification<JobPosting>>any()))
                    .thenReturn(List.of(job));

            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setJavaRelevance("LOW");
            filter.setRequiredGermanLevel("B2");

            jobPostingService.updateStatusByFilter(filter, JobPostingStatus.ANALYZED);

            assertEquals(JobPostingStatus.ANALYZED, job.getStatus());

            verify(jobPostingRepository).findAll(ArgumentMatchers.<Specification<JobPosting>>any());
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }

        @Test
        void shouldNotUpdateJobsWhenLatestAnalysisDoesNotMatch() {
            JobPosting job = new JobPosting("https://a.com", "A", "A", "A", "A");
            job.setId(1L);
            job.setStatus(JobPostingStatus.NEW);

            JobAnalysis olderAnalysis = new JobAnalysis();
            olderAnalysis.setJobPosting(job);
            olderAnalysis.setJavaRelevance("LOW");
            olderAnalysis.setRequiredGermanLevel("B2");
            olderAnalysis.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

            JobAnalysis latestAnalysis = new JobAnalysis();
            latestAnalysis.setJobPosting(job);
            latestAnalysis.setJavaRelevance("HIGH");
            latestAnalysis.setRequiredGermanLevel("C1");
            latestAnalysis.setCreatedAt(java.time.LocalDateTime.now());

            job.getAnalyses().add(olderAnalysis);
            job.getAnalyses().add(latestAnalysis);

            when(jobPostingRepository.findAll(ArgumentMatchers.<Specification<JobPosting>>any()))
                    .thenReturn(List.of(job));

            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setJavaRelevance("LOW");
            filter.setRequiredGermanLevel("B2");

            jobPostingService.updateStatusByFilter(filter, JobPostingStatus.ANALYZED);

            assertEquals(JobPostingStatus.NEW, job.getStatus());

            verify(jobPostingRepository).findAll(ArgumentMatchers.<Specification<JobPosting>>any());
            verifyNoMoreInteractions(jobPostingRepository, jobMetadataFetcher);
        }
    }

}
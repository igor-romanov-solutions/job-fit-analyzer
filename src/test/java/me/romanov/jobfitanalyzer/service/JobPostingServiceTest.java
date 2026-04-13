package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.repository.JobPostingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JobPostingServiceTest {

    private JobPostingRepository jobPostingRepository;
    private JobPostingService jobPostingService;

    @BeforeEach
    void setUp() {
        jobPostingRepository = mock(JobPostingRepository.class);
        jobPostingService = new JobPostingService(jobPostingRepository);
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
}
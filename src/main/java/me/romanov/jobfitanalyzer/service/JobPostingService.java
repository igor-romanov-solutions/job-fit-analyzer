package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.repository.JobPostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    public JobPosting create(CreateJobPostingRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        JobPosting jobPosting = new JobPosting(
                normalize(request.sourceUrl()),
                trimToNull(request.companyName()),
                trimToNull(request.jobTitle()),
                trimToNull(request.location()),
                request.description().trim()
        );

        return jobPostingRepository.save(jobPosting);
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findAll() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findByStatus(JobPostingStatus status) {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public void updateStatus(Long id, JobPostingStatus status) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JobPosting not found: " + id));

        jobPosting.updateStatus(status);
    }

    @Transactional(readOnly = true)
    public JobPosting findById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JobPosting not found: " + id));
    }

    private String normalize(String value) {
        return trimToNull(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
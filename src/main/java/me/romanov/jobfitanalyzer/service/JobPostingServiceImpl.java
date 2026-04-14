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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobMetadataFetcher jobMetadataFetcher;

    public JobPostingServiceImpl(JobPostingRepository jobPostingRepository, JobMetadataFetcher jobMetadataFetcher) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobMetadataFetcher = jobMetadataFetcher;
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

    public JobPosting update(Long id, UpdateJobPostingRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new JobPostingNotFoundException(id));

        jobPosting.setSourceUrl(normalize(request.sourceUrl()));
        jobPosting.setCompanyName(trimToNull(request.companyName()));
        jobPosting.setJobTitle(trimToNull(request.jobTitle()));
        jobPosting.setLocation(trimToNull(request.location()));
        jobPosting.setDescription(request.description().trim());
        jobPosting.updateStatus(request.status());

        return jobPosting;
    }

    public Optional<JobMetadata> fetchMetadata(String sourceUrl) {
        String normalized = trimToNull(sourceUrl);
        if (normalized == null) {
            return Optional.empty();
        }

        return jobMetadataFetcher.fetch(normalized);
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findAll() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<JobPosting> findByStatus(JobPostingStatus status) {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public JobPosting findById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new JobPostingNotFoundException(id));
    }

    @Override
    public List<JobPosting> findByFilter(JobPostingFilterRequest filterRequest) {
        Specification<JobPosting> spec = Specification.allOf();

        if (filterRequest.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filterRequest.getStatus()));
        }

        List<JobPosting> jobs = jobPostingRepository.findAll(spec);

        return jobs.stream()
                .filter(job -> matchesLatestAnalysis(job, filterRequest))
                .sorted(Comparator.comparing(JobPosting::getCreatedAt).reversed())
                .toList();
    }

    private boolean matchesLatestAnalysis(JobPosting job, JobPostingFilterRequest filterRequest) {
        JobAnalysis latestAnalysis = job.getAnalyses().stream()
                .max(Comparator.comparing(JobAnalysis::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        boolean javaRelevanceMatches = filterRequest.getJavaRelevance() == null
                || filterRequest.getJavaRelevance().isBlank()
                || (latestAnalysis != null
                && filterRequest.getJavaRelevance().equals(latestAnalysis.getJavaRelevance()));

        boolean germanLevelMatches = filterRequest.getRequiredGermanLevel() == null
                || filterRequest.getRequiredGermanLevel().isBlank()
                || (latestAnalysis != null
                && filterRequest.getRequiredGermanLevel().equals(latestAnalysis.getRequiredGermanLevel()));

        return javaRelevanceMatches && germanLevelMatches;
    }

    @Override
    public void updateStatusByFilter(JobPostingFilterRequest filterRequest, JobPostingStatus status) {
        Objects.requireNonNull(filterRequest, "filterRequest must not be null");
        Objects.requireNonNull(status, "status must not be null");

        List<JobPosting> jobs = findByFilter(filterRequest);
        jobs.forEach(jobPosting -> jobPosting.updateStatus(status));

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
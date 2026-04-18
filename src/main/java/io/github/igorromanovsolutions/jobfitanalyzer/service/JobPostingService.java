package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPosting;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPostingStatus;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.CreateJobPostingRequest;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobMetadata;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobPostingFilterRequest;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.UpdateJobPostingRequest;

import java.util.List;

public interface JobPostingService {

    JobPosting create(CreateJobPostingRequest request);

    JobPosting update(Long id, UpdateJobPostingRequest request);

    java.util.Optional<JobMetadata> fetchMetadata(String sourceUrl);

    List<JobPosting> findAll();

    List<JobPosting> findByStatus(JobPostingStatus status);

    JobPosting findById(Long id);

    List<JobPosting> findByFilter(JobPostingFilterRequest filterRequest);

    void updateStatusByFilter(JobPostingFilterRequest filterRequest, JobPostingStatus status);
}
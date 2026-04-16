package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.JobPostingFilterRequest;

import java.util.List;

public interface JobPostingService {

    JobPosting create(me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest request);

    JobPosting update(Long id, me.romanov.jobfitanalyzer.dto.UpdateJobPostingRequest request);

    java.util.Optional<me.romanov.jobfitanalyzer.dto.JobMetadata> fetchMetadata(String sourceUrl);

    List<JobPosting> findAll();

    List<JobPosting> findByStatus(JobPostingStatus status);

    JobPosting findById(Long id);

    List<JobPosting> findByFilter(JobPostingFilterRequest filterRequest);

    void updateStatusByFilter(JobPostingFilterRequest filterRequest, JobPostingStatus status);
}
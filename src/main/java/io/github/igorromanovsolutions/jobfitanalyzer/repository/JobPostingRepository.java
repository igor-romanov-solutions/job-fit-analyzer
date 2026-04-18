package io.github.igorromanovsolutions.jobfitanalyzer.repository;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPosting;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPostingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

    List<JobPosting> findAllByOrderByCreatedAtDesc();

    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobPostingStatus status);

}
package me.romanov.jobfitanalyzer.repository;

import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

    List<JobPosting> findAllByOrderByCreatedAtDesc();

    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobPostingStatus status);

}
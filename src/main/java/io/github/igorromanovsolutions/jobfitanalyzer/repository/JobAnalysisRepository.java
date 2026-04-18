package io.github.igorromanovsolutions.jobfitanalyzer.repository;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, Long> {

    Optional<JobAnalysis> findTopByJobPostingIdOrderByCreatedAtDesc(Long jobPostingId);

}

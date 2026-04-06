package me.romanov.jobfitanalyzer.repository;

import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
}

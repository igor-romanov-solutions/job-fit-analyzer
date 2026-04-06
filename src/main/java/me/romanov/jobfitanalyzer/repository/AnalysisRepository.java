package me.romanov.jobfitanalyzer.repository;

import me.romanov.jobfitanalyzer.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
    List<AnalysisEntity> findAllByOrderByCreatedAtDesc();
}

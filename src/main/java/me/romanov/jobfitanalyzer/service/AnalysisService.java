package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;

public interface AnalysisService {

    AnalysisResult analyze(AnalysisRequest request);

    JobAnalysis analyzeAndSave(JobPosting jobPosting, AnalysisRequest request);
}

package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobAnalysis;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPosting;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisRequest;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.AnalysisResult;

public interface AnalysisService {

    AnalysisResult analyze(AnalysisRequest request);

    JobAnalysis analyzeAndSave(JobPosting jobPosting, AnalysisRequest request);
}

package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;

public interface AnalysisService {
    AnalysisResult analyzeAndSave(AnalysisRequest request);
}

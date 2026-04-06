package me.romanov.jobfitanalyzer.service;

import me.romanov.jobfitanalyzer.dto.AnalysisHistoryItemDto;
import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.dto.AnalysisViewDto;

import java.util.List;

public interface AnalysisService {
    AnalysisResult analyzeAndSave(AnalysisRequest request);
    List<AnalysisHistoryItemDto> getHistory();
    AnalysisViewDto getAnalysis(Long id);
}

package me.romanov.jobfitanalyzer.domain;

public class AnalysisFailedException extends RuntimeException {
    public AnalysisFailedException(String message) {
        super(message);
    }
}

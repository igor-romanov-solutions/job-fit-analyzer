package io.github.igorromanovsolutions.jobfitanalyzer.domain;

public class AnalysisFailedException extends RuntimeException {
    public AnalysisFailedException(String message) {
        super(message);
    }
}

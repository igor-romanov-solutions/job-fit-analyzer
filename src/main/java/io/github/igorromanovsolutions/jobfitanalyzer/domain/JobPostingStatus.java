package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import lombok.Getter;

@Getter
public enum JobPostingStatus {
    NEW(0),
    ANALYZED(1),
    MAYBE(2),
    TO_APPLY(3),
    PROCESSED(4),
    REJECTED(5);

    private final int priority;

    JobPostingStatus(int priority) {
        this.priority = priority;
    }
}

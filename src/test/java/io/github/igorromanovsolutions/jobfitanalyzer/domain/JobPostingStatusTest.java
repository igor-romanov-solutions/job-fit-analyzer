package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobPostingStatusTest {

    @Test
    void shouldExposeExpectedPriorities() {
        assertEquals(0, JobPostingStatus.NEW.getPriority());
        assertEquals(1, JobPostingStatus.ANALYZED.getPriority());
        assertEquals(2, JobPostingStatus.MAYBE.getPriority());
        assertEquals(3, JobPostingStatus.TO_APPLY.getPriority());
        assertEquals(4, JobPostingStatus.PROCESSED.getPriority());
        assertEquals(5, JobPostingStatus.REJECTED.getPriority());
    }
}
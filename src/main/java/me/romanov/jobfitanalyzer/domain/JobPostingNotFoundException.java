package me.romanov.jobfitanalyzer.domain;

public class JobPostingNotFoundException extends RuntimeException {

    public JobPostingNotFoundException(Long id) {
        super("JobPosting not found: " + id);
    }
}

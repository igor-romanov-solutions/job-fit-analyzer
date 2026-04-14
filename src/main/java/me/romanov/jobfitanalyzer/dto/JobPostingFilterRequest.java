package me.romanov.jobfitanalyzer.dto;

import lombok.Data;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class JobPostingFilterRequest {

    private JobPostingStatus status;
    private String javaRelevance;
    private String requiredGermanLevel;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdTo;
}
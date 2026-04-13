package me.romanov.jobfitanalyzer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "job_postings")
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "location")
    private String location;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private JobPostingStatus status;

    @Column(name = "analysis_summary", columnDefinition = "TEXT")
    private String analysisSummary;

    @Column(name = "analysis_score")
    private Integer analysisScore;

    @Column(name = "analysis_raw_json", columnDefinition = "TEXT")
    private String analysisRawJson;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public JobPosting(String sourceUrl, String companyName, String jobTitle, String location, String description) {
        this.sourceUrl = sourceUrl;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.location = location;
        this.description = description;
        this.status = JobPostingStatus.NEW;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = JobPostingStatus.NEW;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(JobPostingStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void applyAnalysisResult(String summary, Integer score, String rawJson) {
        this.analysisSummary = summary;
        this.analysisScore = score;
        this.analysisRawJson = rawJson;
        this.analyzedAt = LocalDateTime.now();
        this.status = JobPostingStatus.ANALYZED;
    }
}
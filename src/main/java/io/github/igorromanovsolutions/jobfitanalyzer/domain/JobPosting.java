package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "jobPosting")
    private List<JobAnalysis> analyses = new ArrayList<>();

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
}
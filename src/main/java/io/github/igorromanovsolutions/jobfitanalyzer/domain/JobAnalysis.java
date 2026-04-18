package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "job_analyses")
public class JobAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String roleType;
    private String javaRelevance;
    private String seniorityLevel;
    private String domain;
    private String vacancyLanguage;
    private String requiredGermanLevel;

    @Column(length = 1000)
    private String primaryStack;

    @Column(length = 1000)
    private String secondaryStack;

    @Column(length = 1000)
    private String niceToHaveStack;

    @Column(length = 3000)
    private String gaps;
}

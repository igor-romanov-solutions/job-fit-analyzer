package me.romanov.jobfitanalyzer.service;


import me.romanov.jobfitanalyzer.dto.JobMetadata;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@Tag("integration")
class LinkedinJobMetadataFetcherTest {


    private final JobMetadataFetcher fetcher;

    @Autowired
    LinkedinJobMetadataFetcherTest(JobMetadataFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("jobs")
    void shouldFetchLinkedinVacancy(TestCase tc) {

        Optional<JobMetadata> result = fetcher.fetch(tc.url());

        // если LinkedIn отдал login wall / блок → пропускаем
        assumeTrue(result.isPresent(), "Blocked or empty response: " + tc.url());

        JobMetadata metadata = result.get();

        assertAll(
                () -> assertEquals(tc.company(), metadata.companyName(), "company"),
                () -> assertEquals(tc.title(), metadata.jobTitle(), "title"),
                () -> assertEquals(tc.location(), metadata.location(), "location"),

                () -> assertNotNull(metadata.description(), "description"),
                () -> assertFalse(metadata.description().isBlank(), "description should not be blank"),

                () -> assertTrue(
                        metadata.description().contains(tc.descriptionContains()),
                        "description should contain expected fragment"
                )
        );
    }

    // === MethodSource ===
    static Stream<TestCase> jobs() {
        return Stream.of(
                new TestCase(
                        "https://www.linkedin.com/jobs/view/4368883287/",
                        "adesso Schweiz AG",
                        "Java Spring Boot Software Engineer (all genders)",
                        "Zurich, Zurich, Switzerland",
                        "adesso stands for IT excellence and therefore also for excellent development opportunities for all adessi. We grow together and learn from each other - on our projects, as a team and with outstanding training opportunities. We have IT at heart, the industry at heart and the success of our customers at heart. Because successful business is the result of innovative ideas, sustainable strategies and smart IT solutions."
                ),
                new TestCase(
                        "https://www.linkedin.com/jobs/view/4395361107",
                        "CLEEVEN",
                        "Java Fullstack Engineer",
                        "Zurich, Switzerland",
                        "As a Java Full-Stack Developer, you will join a project team working directly with our clients—on-site or in hybrid mode. You will apply your technical expertise and interpersonal skills to deliver high-impact software solutions within complex, large-scale environments."
                ),
                new TestCase(
                        "https://www.linkedin.com/jobs/view/4395361107",
                        "CLEEVEN",
                        "Java Fullstack Engineer",
                        "Zurich, Switzerland",
                        "As a Java Full-Stack Developer, you will join a project team working directly with our clients—on-site or in hybrid mode. You will apply your technical expertise and interpersonal skills to deliver high-impact software solutions within complex, large-scale environments."
                ),
                new TestCase(
                        "https://www.linkedin.com/jobs/view/4362018896/",
                        "Digital Asset",
                        "Backend Software Engineer - SDK",
                        "Zurich, Switzerland",
                        "As a software engineer in the SDK team, you'll have the unique opportunity to work with cutting-edge technology that spans the entire stack. This includes everything from the database back-end and Pekko Stream implementation, through gRPC and HTTP interfaces, to the Java and TypeScript client libraries."
                )
        );
    }

    // === DTO ===
    record TestCase(
            String url,
            String company,
            String title,
            String location,
            String descriptionContains
    ) {
        @Override
        public @NotNull String toString() {
            return url != null ? url : "null";
        }
    }
}
package io.github.igorromanovsolutions.jobfitanalyzer.service;

import io.github.igorromanovsolutions.jobfitanalyzer.dto.JobMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LinkedinJobMetadataFetcherUnitTest {

    @Test
    void fetch_shouldParseDataFromJsonLd() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@context":"https://schema.org",
                        "@type":"JobPosting",
                        "title":"Java Developer",
                        "description":"<p>Build services</p><ul><li>Spring</li><li>Kafka</li></ul>",
                        "hiringOrganization":{"name":"Acme GmbH"},
                        "jobLocation":{"addressLocality":"Zurich, Switzerland"}
                      }
                    </script>
                  </head>
                  <body></body>
                </html>
                """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        JobMetadata metadata = result.get();
        assertEquals("Acme GmbH", metadata.companyName());
        assertEquals("Java Developer", metadata.jobTitle());
        assertEquals("Zurich, Switzerland", metadata.location());
        assertNotNull(metadata.description());
        assertTrue(metadata.description().contains("Build services"));
        assertTrue(metadata.description().contains("Spring"));
        assertTrue(metadata.description().contains("Kafka"));
    }

    @Test
    void fetch_shouldFallbackToHtmlWhenJsonLdNotPresent() {
        String html = """
                <html>
                  <body>
                    <h1 class="top-card-layout__title">Backend Engineer</h1>
                    <a class="topcard__org-name-link">Example Corp</a>
                    <span class="topcard__flavor--bullet">Berlin, Germany</span>
                    <div class="show-more-less-html__markup">
                      <p>We build distributed systems.</p>
                    </div>
                  </body>
                </html>
                """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        JobMetadata metadata = result.get();
        assertEquals("Example Corp", metadata.companyName());
        assertEquals("Backend Engineer", metadata.jobTitle());
        assertEquals("Berlin, Germany", metadata.location());
        assertEquals("We build distributed systems.", metadata.description());
    }

    @Test
    void fetch_shouldReturnEmpty_whenHtmlContainsNoUsefulData() {
        String html = """
                <html>
                  <body>
                    <div>Nothing useful here</div>
                  </body>
                </html>
                """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isEmpty());
    }

    @Test
    void fetch_shouldReturnEmptyAndPreserveInterruptFlag_whenInterruptedExceptionOccurs() {
        LinkedinJobMetadataFetcher fetcher = new LinkedinJobMetadataFetcher() {
            @Override
            protected String loadHtml(String url) throws InterruptedException {
                throw new InterruptedException("interrupted");
            }
        };

        assertFalse(Thread.currentThread().isInterrupted());

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isEmpty());
        assertTrue(Thread.currentThread().isInterrupted());

        boolean interrupted = Thread.interrupted();  // cleanup interrupt flag for the test thread
        assertTrue(interrupted);
    }

    @Test
    void loadHtml_shouldThrowIllegalStateException_whenHttpStatusIsError() {
        LinkedinJobMetadataFetcher fetcher = new LinkedinJobMetadataFetcher() {
            @Override
            protected String loadHtml(String url) {
                throw new IllegalStateException("HTTP error: 500");
            }
        };

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isEmpty());
    }

    @Test
    void formatElementForAi_shouldPreserveParagraphsAndLists() {
        String html = """
            <html>
              <body>
                <div class="show-more-less-html__markup">
                  <p>First paragraph.</p>
                  <ul>
                    <li>Item one</li>
                    <li>Item two</li>
                  </ul>
                  <p>Second paragraph.</p>
                </div>
              </body>
            </html>
            """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        String description = result.get().description();
        assertNotNull(description);
        assertTrue(description.contains("First paragraph."));
        assertTrue(description.contains("Item one"));
        assertTrue(description.contains("Item two"));
        assertTrue(description.contains("Second paragraph."));
    }

    @Test
    void parseFromJsonLd_shouldUseAddressWhenJobLocationIsMissing() {
        String html = """
                <html>
                  <head>
                    <script type="application/ld+json">
                      {
                        "@type":"JobPosting",
                        "title":"QA Engineer",
                        "description":"<p>Testing stuff</p>",
                        "hiringOrganization":{"name":"Test AG"},
                        "address":{"addressLocality":"Basel, Switzerland"}
                      }
                    </script>
                  </head>
                  <body></body>
                </html>
                """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        assertEquals("Basel, Switzerland", result.get().location());
    }

    private static class StubFetcher extends LinkedinJobMetadataFetcher {
        private final String html;

        private StubFetcher(String html) {
            this.html = html;
            ReflectionTestUtils.setField(this, "httpClient", HttpClient.newHttpClient());
        }

        @Override
        protected String loadHtml(String url) {
            return html;
        }
    }

    @Test
    void formatElementForAi_shouldHandleOrderedList() {
        String html = """
            <html>
              <body>
                <div class="show-more-less-html__markup">
                  <ol>
                    <li>First</li>
                    <li>Second</li>
                  </ol>
                </div>
              </body>
            </html>
            """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        String description = result.get().description();
        assertNotNull(description);
        assertTrue(description.contains("1. First"));
        assertTrue(description.contains("2. Second"));
    }

    @Test
    void fetch_shouldHandleEscapedCharactersInJsonLdDescription() {
        String html = """
            <html>
              <head>
                <script type="application/ld+json">
                  {
                    "@type":"JobPosting",
                    "title":"Java Developer",
                    "description":"Line 1\\nLine 2 with quote: \\"Hello\\" and slash \\\\",
                    "hiringOrganization":{"name":"Acme GmbH"},
                    "jobLocation":{"addressLocality":"Zurich, Switzerland"}
                  }
                </script>
              </head>
              <body></body>
            </html>
            """;

        LinkedinJobMetadataFetcher fetcher = new StubFetcher(html);

        Optional<JobMetadata> result = fetcher.fetch("https://example.com/job");

        assertTrue(result.isPresent());
        String description = result.get().description();
        assertNotNull(description);
        assertTrue(description.contains("Line 1"));
        assertTrue(description.contains("Line 2"));
        assertTrue(description.contains("Hello"));
    }
}

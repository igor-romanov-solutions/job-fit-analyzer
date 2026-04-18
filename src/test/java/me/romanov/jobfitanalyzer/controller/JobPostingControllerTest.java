package me.romanov.jobfitanalyzer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.romanov.jobfitanalyzer.config.OpenAiProperties;
import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.*;
import me.romanov.jobfitanalyzer.repository.JobAnalysisRepository;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import me.romanov.jobfitanalyzer.service.JobPostingService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobPostingController.class)
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private JobPostingService jobPostingService;

    @MockitoBean
    private AnalysisService analysisService;

    @MockitoBean
    private JobAnalysisRepository jobAnalysisRepository;

    @MockitoBean
    private OpenAiProperties openAiProperties;

    @Nested
    class RootTests {

        @Test
        void shouldRedirectRootToJobs() throws Exception {
            mockMvc.perform(get("/jobs/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs"));
        }
    }

    @Nested
    class ListJobsTests {

        @Test
        void shouldReturnJobsListPageWithoutFilter() throws Exception {
            JobPosting job1 = buildJobPosting(1L, "A", JobPostingStatus.NEW);
            JobPosting job2 = buildJobPosting(2L, "B", JobPostingStatus.ANALYZED);

            when(jobPostingService.findByFilter(any(JobPostingFilterRequest.class)))
                    .thenReturn(List.of(job2, job1));

            mockMvc.perform(get("/jobs"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/list"))
                    .andExpect(model().attributeExists("filter"))
                    .andExpect(model().attributeExists("jobs"))
                    .andExpect(model().attributeExists("allStatuses"))
                    .andExpect(model().attribute("jobs", List.of(job2, job1)));

            ArgumentCaptor<JobPostingFilterRequest> captor =
                    ArgumentCaptor.forClass(JobPostingFilterRequest.class);
            verify(jobPostingService).findByFilter(captor.capture());
            assertNull(captor.getValue().getStatus());
            assertNull(captor.getValue().getJavaRelevance());

            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldReturnJobsListPageWithStatusFilter() throws Exception {
            JobPosting job = buildJobPosting(1L, "A", JobPostingStatus.NEW);

            when(jobPostingService.findByFilter(any(JobPostingFilterRequest.class)))
                    .thenReturn(List.of(job));

            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setStatus(JobPostingStatus.NEW);

            mockMvc.perform(get("/jobs").sessionAttr("filter", filter))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/list"))
                    .andExpect(model().attributeExists("filter"))
                    .andExpect(model().attributeExists("jobs"))
                    .andExpect(model().attributeExists("allStatuses"))
                    .andExpect(model().attribute("jobs", List.of(job)));

            ArgumentCaptor<JobPostingFilterRequest> captor =
                    ArgumentCaptor.forClass(JobPostingFilterRequest.class);
            verify(jobPostingService).findByFilter(captor.capture());
            assertEquals(JobPostingStatus.NEW, captor.getValue().getStatus());
            assertNull(captor.getValue().getJavaRelevance());

            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldBulkUpdateStatusForFilteredJobs() throws Exception {
            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setStatus(JobPostingStatus.NEW);
            filter.setJavaRelevance("LOW");

            mockMvc.perform(post("/jobs/bulk-status")
                            .sessionAttr("filter", filter)
                            .param("targetStatus", "ANALYZED"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs?status=NEW&javaRelevance=LOW"));

            ArgumentCaptor<JobPostingFilterRequest> filterCaptor =
                    ArgumentCaptor.forClass(JobPostingFilterRequest.class);
            verify(jobPostingService).updateStatusByFilter(filterCaptor.capture(), eq(JobPostingStatus.ANALYZED));
            assertEquals(JobPostingStatus.NEW, filterCaptor.getValue().getStatus());
            assertEquals("LOW", filterCaptor.getValue().getJavaRelevance());

            verifyNoMoreInteractions(jobPostingService);
        }
    }


    @Nested
    class FilterTests {
        @Test
        void shouldApplyFilterAndRedirectToJobs() throws Exception {
            mockMvc.perform(get("/jobs/filter")
                            .param("status", "NEW")
                            .param("javaRelevance", "LOW"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs"))
                    .andExpect(request().sessionAttribute("filter", org.hamcrest.Matchers.notNullValue()));

            verifyNoInteractions(jobPostingService, analysisService, jobAnalysisRepository);
        }
    }

    @Nested
    class ResetTests {
        @Test
        void shouldResetFilterAndRedirectToJobs() throws Exception {
            JobPostingFilterRequest filter = new JobPostingFilterRequest();
            filter.setStatus(JobPostingStatus.NEW);
            filter.setJavaRelevance("LOW");

            mockMvc.perform(get("/jobs/reset").sessionAttr("filter", filter))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs"));

            verifyNoMoreInteractions(jobPostingService);
        }
    }


    @Nested
    class CreateJobTests {

        @Test
        void shouldShowCreateForm() throws Exception {
            mockMvc.perform(get("/jobs/new"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/create"))
                    .andExpect(model().attributeExists("jobForm"));

            verifyNoInteractions(jobPostingService, analysisService, jobAnalysisRepository);
        }

        @Test
        void shouldCreateJobAndRedirect() throws Exception {
            mockMvc.perform(post("/jobs")
                            .param("sourceUrl", "https://example.com")
                            .param("companyName", "UBS")
                            .param("jobTitle", "Java Dev")
                            .param("location", "Zurich")
                            .param("description", "Some description"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs"));

            verify(jobPostingService).create(any(CreateJobPostingRequest.class));
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldReturnCreatePageWhenValidationFails() throws Exception {
            mockMvc.perform(post("/jobs")
                            .param("sourceUrl", "https://example.com")
                            .param("companyName", "UBS")
                            .param("jobTitle", "Java Dev")
                            .param("location", "Zurich")
                            .param("description", "   "))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/create"))
                    .andExpect(model().hasErrors());

            verifyNoInteractions(jobPostingService, analysisService, jobAnalysisRepository);
        }
    }

    @Nested
    class ViewJobTests {

        @Test
        void shouldShowJobDetailsWithLatestAnalysis() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);
            JobAnalysis analysis = buildAnalysis(id, job);

            when(jobPostingService.findById(id)).thenReturn(job);
            when(jobAnalysisRepository.findTopByJobPostingIdOrderByCreatedAtDesc(id)).thenReturn(Optional.of(analysis));

            mockMvc.perform(get("/jobs/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/details"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().attribute("analysis", analysis));

            verify(jobPostingService).findById(id);
            verify(jobAnalysisRepository).findTopByJobPostingIdOrderByCreatedAtDesc(id);
            verifyNoMoreInteractions(jobPostingService, jobAnalysisRepository);
        }

        @Test
        void shouldShowJobDetailsWithoutAnalysis() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);
            when(jobAnalysisRepository.findTopByJobPostingIdOrderByCreatedAtDesc(id)).thenReturn(Optional.empty());

            mockMvc.perform(get("/jobs/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/details"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().attribute("analysis", (Object) null));

            verify(jobPostingService).findById(id);
            verify(jobAnalysisRepository).findTopByJobPostingIdOrderByCreatedAtDesc(id);
            verifyNoMoreInteractions(jobPostingService, jobAnalysisRepository);
        }
    }

    @Nested
    class EditJobTests {

        @Test
        void shouldShowEditForm() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);

            mockMvc.perform(get("/jobs/{id}/edit", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/edit"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().attributeExists("jobForm"))
                    .andExpect(model().attributeExists("allStatuses"))
                    .andExpect(model().attribute("returnTo", "details"));

            verify(jobPostingService).findById(id);
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldUpdateJobAndRedirectToDetailsByDefault() throws Exception {
            Long id = 1L;

            mockMvc.perform(post("/jobs/{id}", id)
                            .param("sourceUrl", "https://example.com")
                            .param("companyName", "UBS")
                            .param("jobTitle", "Java Dev")
                            .param("location", "Zurich")
                            .param("description", "Updated description")
                            .param("status", "ANALYZED"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs/" + id));

            verify(jobPostingService).update(eq(id), any(UpdateJobPostingRequest.class));
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldUpdateJobAndRedirectToListWhenRequested() throws Exception {
            Long id = 1L;

            mockMvc.perform(post("/jobs/{id}", id)
                            .param("sourceUrl", "https://example.com")
                            .param("companyName", "UBS")
                            .param("jobTitle", "Java Dev")
                            .param("location", "Zurich")
                            .param("description", "Updated description")
                            .param("status", "ANALYZED")
                            .param("returnTo", "list"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs"));

            verify(jobPostingService).update(eq(id), any(UpdateJobPostingRequest.class));
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldReturnEditPageWhenValidationFails() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);

            mockMvc.perform(post("/jobs/{id}", id)
                            .param("sourceUrl", "https://example.com")
                            .param("companyName", "UBS")
                            .param("jobTitle", "Java Dev")
                            .param("location", "Zurich")
                            .param("description", "   ")
                            .param("status", "ANALYZED")
                            .param("returnTo", "list"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/edit"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().attribute("returnTo", "list"))
                    .andExpect(model().attributeExists("allStatuses"))
                    .andExpect(model().hasErrors());

            verify(jobPostingService).findById(id);
            verifyNoMoreInteractions(jobPostingService);
        }
    }

    @Nested
    class MetadataFetchTests {

        @Test
        void shouldReturnMetadataWhenFetcherFindsIt() throws Exception {
            JobMetadata metadata = new JobMetadata("UBS", "Java Dev", "Zurich", "Desc");
            when(jobPostingService.fetchMetadata("https://example.com"))
                    .thenReturn(Optional.of(metadata));

            mockMvc.perform(post("/jobs/metadata-fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new MetadataFetchRequest("https://example.com"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.companyName").value("UBS"))
                    .andExpect(jsonPath("$.jobTitle").value("Java Dev"))
                    .andExpect(jsonPath("$.location").value("Zurich"))
                    .andExpect(jsonPath("$.description").value("Desc"));

            verify(jobPostingService).fetchMetadata("https://example.com");
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldReturnBadRequestWhenMetadataIsMissing() throws Exception {
            when(jobPostingService.fetchMetadata("https://example.com"))
                    .thenReturn(Optional.empty());

            mockMvc.perform(post("/jobs/metadata-fetch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new MetadataFetchRequest("https://example.com"))))
                    .andExpect(status().isBadRequest());

            verify(jobPostingService).fetchMetadata("https://example.com");
            verifyNoMoreInteractions(jobPostingService);
        }
    }

    @Nested
    class AnalyzeJobTests {

        @Test
        void shouldShowAnalyzeForm() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);

            mockMvc.perform(get("/jobs/{id}/analyze", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/analyze"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().attributeExists("jobAnalysisForm"))
                    .andExpect(model().attribute("analysis", (Object) null));

            verify(jobPostingService).findById(id);
            verifyNoMoreInteractions(jobPostingService);
        }

        @Test
        void shouldAnalyzeJobAndRedirectToDetails() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);
            JobAnalysis savedAnalysis = buildAnalysis(id, job);

            when(jobPostingService.findById(id)).thenReturn(job);
            when(analysisService.analyzeAndSave(eq(job), any(AnalysisRequest.class)))
                    .thenReturn(savedAnalysis);

            mockMvc.perform(post("/jobs/{id}/analyze", id)
                            .param("cvText", "Java developer with Spring")
                            .param("returnTo", "details"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/jobs/" + id));

            verify(jobPostingService).findById(id);
            verify(analysisService).analyzeAndSave(eq(job), any(AnalysisRequest.class));
            verifyNoMoreInteractions(jobPostingService, analysisService);
        }

        @Test
        void shouldReturnAnalyzePageWhenValidationFails() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);

            mockMvc.perform(post("/jobs/{id}/analyze", id)
                            .param("cvText", "   "))
                    .andExpect(status().isOk())
                    .andExpect(view().name("jobs/analyze"))
                    .andExpect(model().attribute("job", job))
                    .andExpect(model().hasErrors());

            verify(jobPostingService).findById(id);
            verifyNoMoreInteractions(jobPostingService);
            verifyNoInteractions(analysisService);
        }

        @Test
        void shouldReturnServiceUnavailableWhenAnalysisFails() throws Exception {
            Long id = 1L;
            JobPosting job = buildJobPosting(id, "A", JobPostingStatus.NEW);

            when(jobPostingService.findById(id)).thenReturn(job);
            when(analysisService.analyzeAndSave(eq(job), any(AnalysisRequest.class)))
                    .thenThrow(new me.romanov.jobfitanalyzer.domain.ExternalServiceException("OpenAI error"));

            mockMvc.perform(post("/jobs/{id}/analyze", id)
                            .param("cvText", "Java developer with Spring"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(view().name("errors/service-unavailable"))
                    .andExpect(model().attribute("errorCode", "EXTERNAL_SERVICE_ERROR"))
                    .andExpect(model().attribute("message",
                            "External service is temporarily unavailable. Please try again later."));

            verify(jobPostingService).findById(id);
            verify(analysisService).analyzeAndSave(eq(job), any(AnalysisRequest.class));
            verifyNoMoreInteractions(jobPostingService, analysisService);
        }
    }

    private static JobPosting buildJobPosting(Long id, String title, JobPostingStatus status) {
        JobPosting jobPosting = new JobPosting("https://example.com", "Company", title, "Zurich", "Description");
        jobPosting.setStatus(status);
        jobPosting.setId(id);
        return jobPosting;
    }

    private static JobAnalysis buildAnalysis(Long id, JobPosting jobPosting) {
        JobAnalysis analysis = new JobAnalysis();
        analysis.setId(id);
        analysis.setJobPosting(jobPosting);
        analysis.setRoleType("BACKEND");
        analysis.setJavaRelevance("HIGH");
        analysis.setSeniorityLevel("SENIOR");
        analysis.setDomain("FINTECH");
        analysis.setVacancyLanguage("ENGLISH");
        analysis.setRequiredGermanLevel("B2");
        analysis.setPrimaryStack("Java, Spring");
        analysis.setSecondaryStack("Kafka, PostgreSQL");
        analysis.setNiceToHaveStack("Docker, Kubernetes");
        analysis.setGaps("German language");
        return analysis;
    }
}
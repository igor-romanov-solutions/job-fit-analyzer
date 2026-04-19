package io.github.igorromanovsolutions.jobfitanalyzer.controller;

import io.github.igorromanovsolutions.jobfitanalyzer.config.OpenAiProperties;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobAnalysis;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPosting;
import io.github.igorromanovsolutions.jobfitanalyzer.domain.JobPostingStatus;
import io.github.igorromanovsolutions.jobfitanalyzer.dto.*;
import io.github.igorromanovsolutions.jobfitanalyzer.repository.JobAnalysisRepository;
import io.github.igorromanovsolutions.jobfitanalyzer.service.AnalysisService;
import io.github.igorromanovsolutions.jobfitanalyzer.service.JobPostingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/jobs")
@Slf4j
public class JobPostingController {

    private static final String ALL_STATUSES_ATTRIBUTE = "allStatuses";
    private static final String JOB_ATTRIBUTE = "job";
    private static final String JOBS_ATTRIBUTE = "jobs";
    private static final String JOB_FORM_ATTRIBUTE = "jobForm";
    private static final String ANALYSIS_ATTRIBUTE = "analysis";
    private static final String JOB_ANALYSIS_FORM_ATTRIBUTE = "jobAnalysisForm";
    private static final String FILTER_ATTRIBUTE = "filter";


    private static final String JOBS_CREATE_TEMPLATE = "jobs/create";
    private static final String JOBS_EDIT_TEMPLATE = "jobs/edit";
    private static final String JOBS_ANALYZE_TEMPLATE = "jobs/analyze";

    private static final String REDIRECT_TO_JOBS = "redirect:/jobs";
    private static final String REDIRECT_TO_JOB_DETAILS = "redirect:/jobs/";

    private static final String RETURN_TO_ATTRIBUTE = "returnTo";
    private static final String RETURN_TO_LIST = "list";
    private static final String RETURN_TO_DETAILS = "details";

    private final JobPostingService jobPostingService;
    private final AnalysisService analysisService;
    private final JobAnalysisRepository jobAnalysisRepository;
    private final OpenAiProperties openAiProperties;

    private static final String FILTER_SESSION_ATTRIBUTE = "filter";

    public JobPostingController(JobPostingService jobPostingService, AnalysisService analysisService, JobAnalysisRepository jobAnalysisRepository, OpenAiProperties openAiProperties) {
        this.jobPostingService = jobPostingService;
        this.analysisService = analysisService;
        this.jobAnalysisRepository = jobAnalysisRepository;
        this.openAiProperties = openAiProperties;
    }

    @GetMapping("/")
    public String root() {
        return REDIRECT_TO_JOBS;
    }

    @GetMapping
    public String listJobs(HttpSession session, Model model) {
        JobPostingFilterRequest filter = resolveFilter(session);

        List<JobPosting> jobs = jobPostingService.findByFilter(filter);
        model.addAttribute(FILTER_ATTRIBUTE, filter);
        model.addAttribute(JOBS_ATTRIBUTE, jobs);
        model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());

        boolean openAiConfigured = openAiProperties.apiKey() != null && !openAiProperties.apiKey().isBlank();
        log.debug("OpenAI configuration: {}", openAiConfigured);
        log.debug("OpenAI configuration: {}", openAiProperties.apiKey());
        model.addAttribute("openAiConfigured", openAiConfigured);
        if (!openAiConfigured) {
            model.addAttribute("openAiWarning",
                    "AI analysis is unavailable because OPENAI_API_KEY is not configured.");
        }

        return "jobs/list";
    }

    @GetMapping("/filter")
    public String applyFilter(@ModelAttribute("filter") JobPostingFilterRequest filter,
                              HttpSession session) {
        session.setAttribute(FILTER_SESSION_ATTRIBUTE, filter);
        return REDIRECT_TO_JOBS;
    }

    @GetMapping("/reset")
    public String resetFilter(HttpSession session) {
        session.removeAttribute(FILTER_SESSION_ATTRIBUTE);
        return REDIRECT_TO_JOBS;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute(JOB_FORM_ATTRIBUTE, new CreateJobPostingForm(null, null, null, null, null));
        return JOBS_CREATE_TEMPLATE;
    }

    @PostMapping
    public String createJob(@Valid @ModelAttribute("jobForm") CreateJobPostingForm form,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return JOBS_CREATE_TEMPLATE;
        }

        CreateJobPostingRequest request = new CreateJobPostingRequest(
                form.sourceUrl(),
                form.companyName(),
                form.jobTitle(),
                form.location(),
                form.description()
        );

        jobPostingService.create(request);
        return REDIRECT_TO_JOBS;
    }

    @PostMapping("/bulk-status")
    public String bulkUpdateStatus(@RequestParam("targetStatus") JobPostingStatus targetStatus,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        JobPostingFilterRequest filter = resolveFilter(session);

        jobPostingService.updateStatusByFilter(filter, targetStatus);

        if (filter.getStatus() != null) {
            redirectAttributes.addAttribute("status", filter.getStatus());
        }
        if (filter.getJavaRelevance() != null && !filter.getJavaRelevance().isBlank()) {
            redirectAttributes.addAttribute("javaRelevance", filter.getJavaRelevance());
        }
        if (filter.getRequiredGermanLevel() != null && !filter.getRequiredGermanLevel().isBlank()) {
            redirectAttributes.addAttribute("requiredGermanLevel", filter.getRequiredGermanLevel());
        }

        return REDIRECT_TO_JOBS;
    }

    @GetMapping("/{id:\\d+}")
    public String viewJob(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);
        JobAnalysis analysis = jobAnalysisRepository
                .findTopByJobPostingIdOrderByCreatedAtDesc(id)
                .orElse(null);

        model.addAttribute(JOB_ATTRIBUTE, job);
        model.addAttribute(ANALYSIS_ATTRIBUTE, analysis);
        return "jobs/details";
    }

    @GetMapping("/{id}/edit")
    public String editJob(@PathVariable Long id,
                          @RequestParam(value = "returnTo", required = false, defaultValue = "details") String returnTo,
                          Model model) {
        JobPosting job = jobPostingService.findById(id);

        UpdateJobPostingForm jobForm = new UpdateJobPostingForm(
                job.getSourceUrl(),
                job.getCompanyName(),
                job.getJobTitle(),
                job.getLocation(),
                job.getDescription(),
                job.getStatus()
        );

        model.addAttribute(JOB_ATTRIBUTE, job);
        model.addAttribute(JOB_FORM_ATTRIBUTE, jobForm);
        model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());
        model.addAttribute(RETURN_TO_ATTRIBUTE, sanitizeReturnTo(returnTo));
        return JOBS_EDIT_TEMPLATE;
    }

    @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobForm") UpdateJobPostingForm form,
                            BindingResult bindingResult,
                            Model model,
                            @RequestParam(value = "returnTo", required = false, defaultValue = "details") String returnTo) {
        String safeReturnTo = sanitizeReturnTo(returnTo);

        if (bindingResult.hasErrors()) {
            JobPosting job = jobPostingService.findById(id);
            model.addAttribute(JOB_ATTRIBUTE, job);
            model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());
            model.addAttribute(RETURN_TO_ATTRIBUTE, safeReturnTo);
            return JOBS_EDIT_TEMPLATE;
        }

        UpdateJobPostingRequest request = new UpdateJobPostingRequest(
                form.sourceUrl(),
                form.companyName(),
                form.jobTitle(),
                form.location(),
                form.description(),
                form.status()
        );

        jobPostingService.update(id, request);
        if (RETURN_TO_DETAILS.equals(safeReturnTo)) {
            return REDIRECT_TO_JOB_DETAILS + id;
        } else {
            return REDIRECT_TO_JOBS;
        }
    }

    private String sanitizeReturnTo(String returnTo) {
        if (RETURN_TO_LIST.equals(returnTo) || RETURN_TO_DETAILS.equals(returnTo)) {
            return returnTo;
        }
        return RETURN_TO_DETAILS;
    }

    @PostMapping("/metadata-fetch")
    @ResponseBody
    public ResponseEntity<JobMetadata> fetchMetadata(@RequestBody MetadataFetchRequest request) {
        return jobPostingService.fetchMetadata(request.sourceUrl())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}/analyze")
    public String showAnalyzeForm(@PathVariable Long id,
                                  @RequestParam(value = "returnTo", required = false, defaultValue = "details") String returnTo,
                                  Model model) {
        JobPosting job = jobPostingService.findById(id);

        model.addAttribute(JOB_ATTRIBUTE, job);
        model.addAttribute(JOB_ANALYSIS_FORM_ATTRIBUTE, new JobAnalysisForm());
        model.addAttribute(ANALYSIS_ATTRIBUTE, null);
        model.addAttribute(RETURN_TO_ATTRIBUTE, sanitizeReturnTo(returnTo));

        return JOBS_ANALYZE_TEMPLATE;
    }

    @PostMapping("/{id}/analyze")
    public String analyzeJob(@PathVariable Long id,
                             @Valid @ModelAttribute("jobAnalysisForm") JobAnalysisForm form,
                             BindingResult bindingResult,
                             @RequestParam(value = "returnTo", required = false, defaultValue = "details") String returnTo,
                             Model model) {
        String safeReturnTo = sanitizeReturnTo(returnTo);

        JobPosting job = jobPostingService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute(JOB_ATTRIBUTE, job);
            return JOBS_ANALYZE_TEMPLATE;
        }


        AnalysisRequest request = new AnalysisRequest();
        request.setCandidateProfile(form.getCvText());
        request.setJobPostingDescription(job.getDescription());

        JobAnalysis jobAnalysis = analysisService.analyzeAndSave(job, request);

        model.addAttribute(JOB_ATTRIBUTE, job);
        model.addAttribute(ANALYSIS_ATTRIBUTE, jobAnalysis);

        if (RETURN_TO_DETAILS.equals(safeReturnTo)) {
            return REDIRECT_TO_JOB_DETAILS + id;
        } else {
            return REDIRECT_TO_JOBS;
        }
    }

    private JobPostingFilterRequest resolveFilter(HttpSession session) {
        Object sessionFilter = session.getAttribute(FILTER_SESSION_ATTRIBUTE);
        if (sessionFilter instanceof JobPostingFilterRequest storedFilter) {
            return storedFilter;
        }
        return new JobPostingFilterRequest();
    }
}
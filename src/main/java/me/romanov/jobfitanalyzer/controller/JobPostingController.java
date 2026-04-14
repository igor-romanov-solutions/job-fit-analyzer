package me.romanov.jobfitanalyzer.controller;

import jakarta.validation.Valid;
import me.romanov.jobfitanalyzer.domain.JobAnalysis;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.*;
import me.romanov.jobfitanalyzer.repository.JobAnalysisRepository;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import me.romanov.jobfitanalyzer.service.JobPostingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {

    private static final String ALL_STATUSES_ATTRIBUTE = "allStatuses";
    private static final String JOB_ATTRIBUTE = "job";
    private static final String JOBS_ATTRIBUTE = "jobs";
    private static final String JOB_FORM_ATTRIBUTE = "jobForm";
    private static final String SELECTED_STATUS_ATTRIBUTE = "selectedStatus";

    private static final String ANALYSIS_ATTRIBUTE = "analysis";
    private static final String JOB_ANALYSIS_FORM_ATTRIBUTE = "jobAnalysisForm";

    private static final String JOBS_CREATE_TEMPLATE = "jobs/create";
    private static final String JOBS_EDIT_TEMPLATE = "jobs/edit";
    private static final String JOBS_ANALYZE_TEMPLATE = "jobs/analyze";

    private final JobPostingService jobPostingService;
    private final AnalysisService analysisService;
    private final JobAnalysisRepository jobAnalysisRepository;

    public JobPostingController(JobPostingService jobPostingService, AnalysisService analysisService, JobAnalysisRepository jobAnalysisRepository) {
        this.jobPostingService = jobPostingService;
        this.analysisService = analysisService;
        this.jobAnalysisRepository = jobAnalysisRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/jobs";
    }

    @GetMapping
    public String listJobs(@RequestParam(value = "status", required = false) JobPostingStatus status,
                           Model model) {
        List<JobPosting> jobs = (status == null)
                ? jobPostingService.findAll()
                : jobPostingService.findByStatus(status);

        model.addAttribute(JOBS_ATTRIBUTE, jobs);
        model.addAttribute(SELECTED_STATUS_ATTRIBUTE, status);
        model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());

        return "jobs/list";
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
        return "redirect:/jobs";
    }

    @GetMapping("/{id}")
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
    public String editJob(@PathVariable Long id, Model model) {
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

        return JOBS_EDIT_TEMPLATE;
    }

    @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobForm") UpdateJobPostingForm form,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            JobPosting job = jobPostingService.findById(id);
            model.addAttribute(JOB_ATTRIBUTE, job);
            model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());
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
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/metadata-fetch")
    @ResponseBody
    public ResponseEntity<JobMetadata> fetchMetadata(@RequestBody MetadataFetchRequest request) {
        return jobPostingService.fetchMetadata(request.sourceUrl())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}/analyze")
    public String showAnalyzeForm(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);

        model.addAttribute(JOB_ATTRIBUTE, job);
        model.addAttribute(JOB_ANALYSIS_FORM_ATTRIBUTE, new JobAnalysisForm());
        model.addAttribute(ANALYSIS_ATTRIBUTE, null);

        return JOBS_ANALYZE_TEMPLATE;
    }

    @PostMapping("/{id}/analyze")
    public String analyzeJob(@PathVariable Long id,
                             @Valid @ModelAttribute("jobAnalysisForm") JobAnalysisForm form,
                             BindingResult bindingResult,
                             Model model) {
        JobPosting job = jobPostingService.findById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute(JOB_ATTRIBUTE, job);
            return JOBS_ANALYZE_TEMPLATE;
        }

        try {
            AnalysisRequest request = new AnalysisRequest();
            request.setCandidateProfile(form.getCvText());
            request.setJobPostingDescription(job.getDescription());

            JobAnalysis jobAnalysis = analysisService.analyzeAndSave(job, request);

            model.addAttribute(JOB_ATTRIBUTE, job);
            model.addAttribute(ANALYSIS_ATTRIBUTE, jobAnalysis);

            return "jobs/details";
        } catch (Exception e) {
            model.addAttribute(JOB_ATTRIBUTE, job);
            model.addAttribute("error", "Analysis failed: " + e.getMessage());
            return JOBS_ANALYZE_TEMPLATE;
        }
    }
}
package me.romanov.jobfitanalyzer.controller;

import jakarta.validation.Valid;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.dto.UpdateJobPostingRequest;
import me.romanov.jobfitanalyzer.service.JobPostingService;
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

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
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
        model.addAttribute(JOB_FORM_ATTRIBUTE, new CreateJobPostingRequest(null, null, null, null, null));
        return "jobs/create";
    }

    @PostMapping
    public String createJob(@Valid @ModelAttribute("jobForm") CreateJobPostingRequest request,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "jobs/create";
        }

        jobPostingService.create(request);
        return "redirect:/jobs";
    }

    @GetMapping("/{id}")
    public String viewJob(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);
        model.addAttribute(JOB_ATTRIBUTE, job);
        return "jobs/details";
    }

    @GetMapping("/{id}/edit")
    public String editJob(@PathVariable Long id, Model model) {
        JobPosting job = jobPostingService.findById(id);

        UpdateJobPostingRequest jobForm = new UpdateJobPostingRequest(
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

        return "jobs/edit";
    }

    @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobForm") UpdateJobPostingRequest request,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            JobPosting job = jobPostingService.findById(id);
            model.addAttribute(JOB_ATTRIBUTE, job);
            model.addAttribute(ALL_STATUSES_ATTRIBUTE, JobPostingStatus.values());
            return "jobs/edit";
        }

        jobPostingService.update(id, request);
        return "redirect:/jobs/" + id;
    }
}
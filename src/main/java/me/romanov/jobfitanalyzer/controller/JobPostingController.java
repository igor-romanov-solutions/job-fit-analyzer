package me.romanov.jobfitanalyzer.controller;

import jakarta.validation.Valid;
import me.romanov.jobfitanalyzer.domain.JobPosting;
import me.romanov.jobfitanalyzer.domain.JobPostingStatus;
import me.romanov.jobfitanalyzer.dto.CreateJobPostingRequest;
import me.romanov.jobfitanalyzer.service.JobPostingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobPostingController {

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

        model.addAttribute("jobs", jobs);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", JobPostingStatus.values());

        return "jobs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("jobForm", new CreateJobPostingRequest(null, null, null, null, null));
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
        model.addAttribute("job", job);
        model.addAttribute("allStatuses", JobPostingStatus.values());
        return "jobs/details";
    }
}

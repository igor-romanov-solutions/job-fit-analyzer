package me.romanov.jobfitanalyzer.controller;

import me.romanov.jobfitanalyzer.dto.AnalysisRequest;
import me.romanov.jobfitanalyzer.dto.AnalysisResult;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import me.romanov.jobfitanalyzer.service.AnalysisServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisServiceImpl analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("analysisRequest", new AnalysisRequest());
        return "index";
    }

    @PostMapping("/")
    public String analyze(@ModelAttribute AnalysisRequest request, Model model) {
        try {
            AnalysisResult result = analysisService.analyzeAndSave(request);
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("error", "Analysis failed: " + e.getMessage());
        }

        model.addAttribute("analysisRequest", request);
        return "index";
    }
}

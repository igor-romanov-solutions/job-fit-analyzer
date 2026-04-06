package me.romanov.jobfitanalyzer.controller;

import lombok.RequiredArgsConstructor;
import me.romanov.jobfitanalyzer.service.AnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    public String getHistory(Model model) {
        model.addAttribute("analyses", analysisService.getHistory());
        return "analysis-history";
    }

    @GetMapping("/{id}")
    public String getAnalysis(@PathVariable Long id, Model model) {
        model.addAttribute("analysis", analysisService.getAnalysis(id));
        return "analysis-details";
    }
}
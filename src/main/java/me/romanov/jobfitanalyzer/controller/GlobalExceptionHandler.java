package me.romanov.jobfitanalyzer.controller;

import lombok.extern.slf4j.Slf4j;
import me.romanov.jobfitanalyzer.domain.AnalysisFailedException;
import me.romanov.jobfitanalyzer.domain.ExternalServiceException;
import me.romanov.jobfitanalyzer.domain.JobPostingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_VIEW = "errors/error";
    private static final String MESSAGE_ATTRIBUTE = "message";
    private static final String ERROR_CODE_ATTRIBUTE = "errorCode";

    @ExceptionHandler(JobPostingNotFoundException.class)
    public ModelAndView handleJobPostingNotFound(JobPostingNotFoundException ex) {
        ModelAndView mav = new ModelAndView("errors/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject(MESSAGE_ATTRIBUTE, ex.getMessage() != null ? ex.getMessage() : "Job posting not found");
        mav.addObject(ERROR_CODE_ATTRIBUTE, "JOB_POSTING_NOT_FOUND");
        return mav;
    }

    @ExceptionHandler({
            ExternalServiceException.class,
            AnalysisFailedException.class
    })
    public ModelAndView handleExternalServiceErrors(RuntimeException ex) {
        log.warn("External service error: {}", ex.getMessage(), ex);

        ModelAndView mav = new ModelAndView("errors/service-unavailable");
        mav.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
        mav.addObject(MESSAGE_ATTRIBUTE, "External service is temporarily unavailable. Please try again later.");
        mav.addObject(ERROR_CODE_ATTRIBUTE, "EXTERNAL_SERVICE_ERROR");
        return mav;
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class
    })
    public ModelAndView handleValidationErrors(Exception ex) {
        ModelAndView mav = new ModelAndView("errors/bad-request");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject(MESSAGE_ATTRIBUTE, "Validation failed. Please check the submitted data.");
        mav.addObject(ERROR_CODE_ATTRIBUTE, "VALIDATION_ERROR");

        if (ex instanceof BindException bindException) {
            mav.addObject("errors", bindException.getBindingResult().getAllErrors());
        }

        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);

        ModelAndView mav = new ModelAndView(ERROR_VIEW);
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject(MESSAGE_ATTRIBUTE, "Something went wrong. Please try again later.");
        mav.addObject(ERROR_CODE_ATTRIBUTE, "INTERNAL_SERVER_ERROR");
        return mav;
    }
}
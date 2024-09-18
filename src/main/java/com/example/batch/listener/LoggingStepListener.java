package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[StepExecutionListener#beforeStep] stepExecution is " + stepExecution.getStatus());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[StepExecutionListener#afterStep] stepExecution is " + stepExecution.getStatus());
        return stepExecution.getExitStatus();
    }
}

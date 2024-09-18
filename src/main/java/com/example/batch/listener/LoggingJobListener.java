package com.example.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingJobListener implements JobExecutionListener {


    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("[JobExecutionListener#beforeJob] jobExecution is " + jobExecution.getStatus());
    }


    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("[JobExecutionListener#afterJob] jobExecution is " + jobExecution.getStatus());
        }

        log.info("[JobExecutionListener#afterJob] jobExecution is " + jobExecution.getStatus());
    }
}

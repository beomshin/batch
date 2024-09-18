package com.example.batch.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobLauncher jobLauncher;
    private final Job myJob;


    @GetMapping("/job/run")
    public void runJob() throws Exception {
        log.info("Job started");
        jobLauncher.run(myJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }
}

package com.example.batch.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AsyncJobController {

    private final JobLauncher jobLauncher;
    private final Job asyncJob;
    private final Job multiThreadStepJob;
    private final Job parallelStepJob;

    @GetMapping("/async/job/run")
    public void runJob() throws Exception {
        log.info("병렬수항테스트(AsyncItemprocessor) 시작");
        jobLauncher.run(asyncJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }


    @GetMapping("/async/job2/run")
    public void runJo2() throws Exception {
        log.info("병렬수항테스트(MultiThreadStepTestJob) 시작");
        jobLauncher.run(multiThreadStepJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/async/job3/run")
    public void runJo3() throws Exception {
        log.info("병렬수항테스트(parallelStepJob) 시작");
        jobLauncher.run(parallelStepJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }
}

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
public class AsyncJobController {

    private final JobLauncher jobLauncher;
    private final Job asyncJob;
    private final Job multiThreadStepJob;
    private final Job parallelStepJob;
    private final Job partitioningJob;

    @GetMapping("/async-item-processor")
    public void asyncItemProcessor() throws Exception {
        log.info("병렬수행테스트(AsyncItemprocessor) 시작");
        jobLauncher.run(asyncJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }


    @GetMapping("/multi-thread")
    public void multiThread() throws Exception {
        log.info("병렬수행테스트(MultiThreadStepTestJob) 시작");
        jobLauncher.run(multiThreadStepJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/parallel-step")
    public void parallelStep() throws Exception {
        log.info("병렬수행테스트(parallelStepJob) 시작");
        jobLauncher.run(parallelStepJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/partitioning")
    public void partitioning() throws Exception {
        log.info("병렬수행테스트(partitioningJob) 시작");
        jobLauncher.run(partitioningJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }
}

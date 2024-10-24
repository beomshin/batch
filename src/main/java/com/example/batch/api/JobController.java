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
    private final Job defaultJob;
    private final Job jpaJob;
    private final Job retryJob;
    private final Job skipJob;

    @GetMapping("/default/job")
    public void defaultJob() throws Exception {
        log.info("배치수행테스트(DefaultJob) 시작");
        jobLauncher.run(defaultJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/jpa/job")
    public void jpaJob() throws Exception {
        log.info("배치수행테스트(JpaJob) 시작");
        jobLauncher.run(jpaJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/retry/job")
    public void retryJob() throws Exception {
        log.info("배치수행테스트(retryJob) 시작");
        jobLauncher.run(retryJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

    @GetMapping("/skip/job")
    public void runJob4() throws Exception {
        log.info("배치수행테스트(skipJob) 시작");
        jobLauncher.run(skipJob, new JobParametersBuilder()
                .addDate("startTime", new Date())  // 고유한 파라미터
                .toJobParameters());
    }

}

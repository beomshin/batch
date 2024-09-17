package com.example.batch.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyJobConfig {

    @Bean
    public Job myJob(JobRepository jobRepository) {
        return new JobBuilder("myJob", jobRepository)
                .start(myStep(jobRepository))
                .build();
    }

    @Bean
    public Step myStep(JobRepository jobRepository) {
        return new StepBuilder("myStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                        log.info("step started");
                        return RepeatStatus.FINISHED;
                }))
                .build();
    }
}

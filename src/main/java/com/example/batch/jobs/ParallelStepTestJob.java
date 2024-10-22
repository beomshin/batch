package com.example.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ParallelStepTestJob {

    @Bean
    public Job parallelStepJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("parallelStepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flow1(jobRepository, batchTransactionManager))
                .split(taskExecutor())
                .add(flow2(jobRepository, batchTransactionManager))
                .next(flow3(jobRepository, batchTransactionManager))
                .end().build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() { // 스레드수 설정
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setCorePoolSize(8);
        return executor;
    }

    public Flow flow1(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {

        return new FlowBuilder<Flow>("flow1")
                .start(step1(jobRepository, batchTransactionManager))
                .build();
    }

    public Flow flow2(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {

        return new FlowBuilder<Flow>("flow2")
                .start(step2(jobRepository, batchTransactionManager))
                .next(step3(jobRepository, batchTransactionManager))
                .build();
    }

    public Flow flow3(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {

        return new FlowBuilder<Flow>("flow3")
                .start(step4(jobRepository, batchTransactionManager))
                .build();
    }


    public Step step1(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(3000);
                    log.info("step1 myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    public Step step2(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    log.info("step2 myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    public Step step3(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("step3", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(6000);
                    log.info("step3 myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    public Step step4(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("step4", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    log.info("step4 myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

}

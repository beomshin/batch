package com.example.batch.jobs.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
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
@RequiredArgsConstructor
public class ParallelStepTestJob {

    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;

    @Bean
    public Job parallelStepJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("parallelStepJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // run.id 증가 설정
                .listener(jobExecutionListener)
                .start(firstFlow(jobRepository, batchTransactionManager)) // 첫번째 병렬 flow
                .split(taskExecutor())
                .add(secondFlow(jobRepository, batchTransactionManager)) // 두번째 병렬 flow
                .next(thirdFlow(jobRepository, batchTransactionManager)) // 세번째 병렬 flow
                .end().build();
    }

    @Bean
    @JobScope
    public ThreadPoolTaskExecutor taskExecutor() { // 스레드수 설정
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setCorePoolSize(8);
        return executor;
    }

    @Bean
    public Flow firstFlow(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new FlowBuilder<Flow>("firstFlow")
                .start(firstStep(jobRepository, batchTransactionManager)) // 스텝 1
                .build();
    }

    @Bean
    public Flow secondFlow(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new FlowBuilder<Flow>("secondFlow")
                .start(secondStep(jobRepository, batchTransactionManager)) // 스텝2 (순차)
                .next(thirdStep(jobRepository, batchTransactionManager)) // 스텝 3  (순차)
                .build();
    }

    @Bean
    public Flow thirdFlow(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new FlowBuilder<Flow>("thirdFlow")
                .start(fourthStep(jobRepository, batchTransactionManager)) // 스텝4
                .build();
    }

    @Bean
    public Step firstStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) { // flow 방식은 scope 설정하지않음
        return new StepBuilder("firstStep", jobRepository)
                .listener(stepExecutionListener)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(3000);
                    log.info("firstStep myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    @Bean
    public Step secondStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("secondStep", jobRepository)
                .listener(stepExecutionListener)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    log.info("secondStep myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    @Bean
    public Step thirdStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("thirdStep", jobRepository)
                .listener(stepExecutionListener)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(6000);
                    log.info("thirdStep myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    @Bean
    public Step fourthStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("fourthStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    log.info("fourthStep myTaskletStep started =============");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

}

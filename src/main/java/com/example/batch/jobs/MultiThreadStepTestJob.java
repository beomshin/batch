package com.example.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Slf4j
@Configuration
public class MultiThreadStepTestJob {

    @Bean
    public Job multiThreadStepJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("multiThreadStepJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(multiThreadStepStep(jobRepository, batchTransactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step multiThreadStepStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("multiThreadStepStep", jobRepository)
                .<String, String>chunk(2, batchTransactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .taskExecutor(asyncTaskExecutor2())
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor asyncTaskExecutor2() { // 스레드수 설정
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4); // 기본 스레드 풀 크기
        executor.setMaxPoolSize(8); // 4개의 스레드가 이미 처리중인데 작업이 더 있을 경우 몇개까지 스레드를 늘릴 것인지
        executor.setThreadNamePrefix("async-thread"); // 스레드 이름 prefix
        return executor;
    }

    public ItemReader<String> itemReader() {
        return new ListItemReader<>(Arrays.asList("a", "b", "c"));
    }

    public ItemProcessor<String, String> itemProcessor() {
        return item -> {
            Thread.sleep(2000);
            return item.toUpperCase();
        };
    }

    public ItemWriter<String> itemWriter() {
        return item -> {
            log.info("Writing item {}", item.getItems());
        };
    }
}

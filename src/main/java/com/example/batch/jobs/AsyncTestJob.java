package com.example.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.concurrent.Future;

@Slf4j
@Configuration
public class AsyncTestJob {

    @Bean
    public Job asyncJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("asyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(asyncProcessorStep(jobRepository, batchTransactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step asyncProcessorStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("asyncProcessorStep", jobRepository)
                .<String, Future<String>>chunk(2, batchTransactionManager)
                .reader(itemReader())
                .processor(asyncProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor asyncTaskExecutor() { // 스레드수 설정
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setCorePoolSize(8);
        return executor;
    }

    @Bean
    public ItemReader<String> itemReader() { // item reader 병렬수행하지 않음
        return new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6"));
    }

    @Bean
    public AsyncItemProcessor<String, String> asyncProcessor() {
        AsyncItemProcessor<String, String> processor = new AsyncItemProcessor<>();
        processor.setTaskExecutor(asyncTaskExecutor());
        processor.setDelegate((item -> { // processor 병렬 수행 위임
            Thread.sleep(2000);
            return item.toUpperCase();
        }));

        return processor;
    }


    @Bean
    public AsyncItemWriter<String> asyncItemWriter() {
        AsyncItemWriter<String> writer = new AsyncItemWriter<>();
        writer.setDelegate((item) -> { // writer 병렬 수행 위임
            log.info("Writing item {}", item.getItems());
        });
        return writer;
    }

}

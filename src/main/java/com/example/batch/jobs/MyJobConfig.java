package com.example.batch.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyJobConfig {

    private final JobExecutionListener jobExecutionListener;

    private final StepExecutionListener stepExecutionListener;

    @Bean
    public Job myJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("myJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .start(myTaskletStep(jobRepository, batchTransactionManager))
                .next(myChunkStep(jobRepository, batchTransactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step myTaskletStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("myTaskletStep", jobRepository)
                .listener(stepExecutionListener)
                .tasklet(((contribution, chunkContext) -> {
                    log.info("myTaskletStep started");
                    return RepeatStatus.FINISHED;
                }), batchTransactionManager).build();
    }

    @Bean
    @JobScope
    public Step myChunkStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("myChunkStep", jobRepository)
                .listener(stepExecutionListener)
                .<String, String >chunk(2, batchTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> reader() {
        return new ListItemReader<>(Arrays.asList("item1", "item2", "item3"));
    }

    @Bean
    @StepScope
    public ItemProcessor<String, String> processor() {
        return item -> {
            log.info("processor started");
            item = item.toUpperCase();
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<? super Object> writer() {
        return items -> {
            log.info("writer started");
            items.forEach(e -> log.info(e.toString()));
        };
    }

}

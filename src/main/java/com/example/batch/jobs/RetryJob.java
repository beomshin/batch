package com.example.batch.jobs;

import com.example.batch.entity.domain.BoardTbEntity;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

@Slf4j
@Configuration
public class RetryJob {

    private final JobExecutionListener jobExecutionListener;

    private final StepExecutionListener stepExecutionListener;

    private final EntityManagerFactory entityManagerFactory;

    private final PlatformTransactionManager transactionManager;

    public RetryJob(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory,
            @Qualifier("loggingStepListener")StepExecutionListener stepExecutionListener,
            @Qualifier("loggingJobListener") JobExecutionListener jobExecutionListener,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager
    ) {
        this.entityManagerFactory = entityManagerFactory;
        this.stepExecutionListener = stepExecutionListener;
        this.jobExecutionListener = jobExecutionListener;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job retryJobb(JobRepository jobRepository) {
        return new JobBuilder("retryJobb", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener)
                .start(retryStep(jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step retryStep(JobRepository jobRepository) {
        return new StepBuilder("retryStep", jobRepository)
                .listener(stepExecutionListener)
                .<BoardTbEntity, BoardTbEntity>chunk(10, transactionManager)
                .reader(retryJpaPagingItemReader())
                .processor(retryProcessor())
                .writer(retryWriter())
                .faultTolerant()
                .retry(SQLException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<BoardTbEntity> retryJpaPagingItemReader() {
        log.info("JpaPagingItemReader start");
        JpaPagingItemReader<BoardTbEntity> pagingItemReader = new JpaPagingItemReader<>();
        pagingItemReader.setQueryString("SELECT b FROM BoardTb b where DATE_FORMAT(regDt, '%y%m%d') != DATE_FORMAT(now(), '%y%m%d') and postType NOT IN (99, 98)");
        pagingItemReader.setEntityManagerFactory(entityManagerFactory);
        pagingItemReader.setPageSize(10);
        return pagingItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<BoardTbEntity, BoardTbEntity> retryProcessor() {
        return board -> {
            BoardTbEntity newBoard = new BoardTbEntity();
            newBoard.setCommentCount(board.getCommentCount());
            newBoard.setLineType(board.getLineType());
            newBoard.setPostType(board.getPostType());
            newBoard.setWriterType(board.getWriterType());
            newBoard.setStatus(board.getStatus());
            newBoard.setView(board.getView());
            newBoard.setContentEmbedded(board.getContentEmbedded());
            newBoard.setReport(board.getReport());
            newBoard.setRecommendCount(board.getRecommendCount());
            if (board.getContentEmbedded().getIp().startsWith("1.111.111.111")) {
                log.info("강제 SQLException 발생");
                throw new SQLException();
            }
            return newBoard;
        };
    }


    @Bean
    @StepScope
    public ItemWriter<BoardTbEntity> retryWriter() {
        log.info("domainWriter start");
        JpaItemWriter<BoardTbEntity> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        jpaItemWriter.setClearPersistenceContext(true);
        return jpaItemWriter;
    }

}

package com.example.batch.jobs.sync;

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

@Slf4j
@Configuration
public class JpaTestJob {

    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final int CHUNK_SIZE = 10;

    public JpaTestJob(
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
    public Job jpaJob(JobRepository jobRepository) {
        return new JobBuilder("jpaJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // run.id 증가 설정
                .listener(jobExecutionListener)
                .start(jpaStep(jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step jpaStep(JobRepository jobRepository) {
        return new StepBuilder("jpaStep", jobRepository)
                .listener(stepExecutionListener)
                .<BoardTbEntity, BoardTbEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(jpaPagingItemReader())
                .processor(jpaProcessor())
                .writer(jpaWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<BoardTbEntity> jpaPagingItemReader() {
        JpaPagingItemReader<BoardTbEntity> pagingItemReader = new JpaPagingItemReader<>();
        pagingItemReader.setQueryString("SELECT b FROM BoardTb b where DATE_FORMAT(regDt, '%y%m%d') != DATE_FORMAT(now(), '%y%m%d') and postType not in (99, 98)");
        pagingItemReader.setEntityManagerFactory(entityManagerFactory);
        pagingItemReader.setPageSize(CHUNK_SIZE); // 청크 사이즈 == 페이징 사이즈 맞추는것이 잠재적 오류 예방
        return pagingItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<BoardTbEntity, BoardTbEntity> jpaProcessor() {
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
            return newBoard;
        };
    }


    @Bean
    @StepScope
    public ItemWriter<BoardTbEntity> jpaWriter() {
        JpaItemWriter<BoardTbEntity> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        jpaItemWriter.setClearPersistenceContext(true);
        return jpaItemWriter;
    }

}

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

import java.sql.SQLException;

@Slf4j
@Configuration
public class SkipTestJob {

    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager transactionManager;
    private final int CHUNK_SIZE = 10;

    public SkipTestJob(
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
    public Job skipJob(JobRepository jobRepository) {
        return new JobBuilder("skipJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // run.id 증가 설정
                .listener(jobExecutionListener)
                .start(skipStep(jobRepository))
                .build();
    }

    @Bean
    @JobScope
    public Step skipStep(JobRepository jobRepository) {
        return new StepBuilder("skipStep", jobRepository)
                .listener(stepExecutionListener)
                .<BoardTbEntity, BoardTbEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(skipJpaPagingItemReader())
                .processor(skipProcessor())
                .writer(skipWriter())
                .faultTolerant() //  내결함성 기능 활성화
                .skipLimit(3) //  skip 허용 횟수, 해당 횟수 초과 시 Error 발생, Skip 사용시 필수 선언
                .skip(SQLException.class) //  SQLException 에 대해서는 skip
                .noSkip(NullPointerException.class) //  NullPointerException 에 대해서는 skip 하지 않음
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<BoardTbEntity> skipJpaPagingItemReader() {
        log.info("JpaPagingItemReader start");
        JpaPagingItemReader<BoardTbEntity> pagingItemReader = new JpaPagingItemReader<>();
        pagingItemReader.setQueryString("SELECT b FROM BoardTb b where DATE_FORMAT(regDt, '%y%m%d') != DATE_FORMAT(now(), '%y%m%d') and postType NOT IN (99, 98)");
        pagingItemReader.setEntityManagerFactory(entityManagerFactory);
        pagingItemReader.setPageSize(CHUNK_SIZE); // 청크 사이즈 == 페이징 사이즈 맞추는것이 잠재적 오류 예방
        return pagingItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<BoardTbEntity, BoardTbEntity> skipProcessor() {
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
            if (board.getContentEmbedded().getIp() != null && board.getContentEmbedded().getIp().startsWith("1.111.111.111")) {
                log.info("강제 SQLException 발생 후 SKIP");
                throw new SQLException();
            }
            return newBoard;
        };
    }


    @Bean
    @StepScope
    public ItemWriter<BoardTbEntity> skipWriter() {
        JpaItemWriter<BoardTbEntity> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        jpaItemWriter.setClearPersistenceContext(true);
        return jpaItemWriter;
    }

}

package com.example.batch.jobs.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PartitioningTestJob {

    private final JobExecutionListener jobExecutionListener;
    private final StepExecutionListener stepExecutionListener;
    private final int CHUNK_SIZE = 3;

    @Bean
    public Job partitioningJob(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new JobBuilder("partitioningJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // run.id 증가 설정
                .listener(jobExecutionListener)
                .start(partitioningStep(jobRepository, batchTransactionManager))
                .build();
    }

    @Bean
    @JobScope
    public ThreadPoolTaskExecutor partitioningTaskExecutor() { // 스레드수 설정
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setCorePoolSize(12);
        return executor;
    }

    @Bean
    @JobScope
    public Step partitioningStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("partitioningStep", jobRepository)
                .partitioner(partitioningSubStep(jobRepository, batchTransactionManager).getName(), new ColumnRangePartitioner()) // 파티션 이름, 클라이언트 정의 파티션 등록
                .step(partitioningSubStep(jobRepository, batchTransactionManager)) // 스텝
                .listener(stepExecutionListener)
                .gridSize(12) // 파티션 개수 ( 스레드 개수와 맞추는것이 좋음)
                .taskExecutor(partitioningTaskExecutor()) // 스레드 생성
                .build();
    }

    // 파티션 step bean 등록시 중복 실행되는 버그로 빈등록 하지 않음

    public Step partitioningSubStep(JobRepository jobRepository, PlatformTransactionManager batchTransactionManager) {
        return new StepBuilder("partitioningSubStep", jobRepository)
                .<String, String>chunk(CHUNK_SIZE, batchTransactionManager) // 최대 읽을 수 있는 아이템 개수 설정
                .listener(stepExecutionListener)
                .reader(partitioningItemReader())
                .processor(partStringItemProcessor())
                .writer(partitioningItemWriter())
                .build();
    }


    public ItemReader<String> partitioningItemReader() {
        return new ListItemReader<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    }


    public ItemProcessor<String, String> partStringItemProcessor() {
        return String::toUpperCase;
    }

    public ItemWriter<String> partitioningItemWriter() {
        return item -> {
            log.info("Writing item {}", item.getItems());
        };
    }

    public static class ColumnRangePartitioner implements Partitioner {

        private final int min = 0;
        private final int max = 36; // 최대값 (예: 데이터의 총 크기)

        @Override
        public Map<String, ExecutionContext> partition(int gridSize) {
            log.info("Partitioning grid size {}", gridSize);
            Map<String, ExecutionContext> result = new HashMap<>();

            // 각 파티션의 범위 계산 (총 범위를 gridSize로 나눈다)
            int targetSize = (max - min) / gridSize;

            int start = min;
            int end = start + targetSize - 1;

            // 파티션별로 ExecutionContext 생성
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();

                // 파티션에 대한 정보를 ExecutionContext에 저장
                context.putInt("minValue", start);
                context.putInt("maxValue", end);

                // 파티션 이름 지정 (partition0, partition1, ...)
                result.put("partition" + i, context);

                // 다음 파티션의 범위 계산
                start = end + 1;
                end = i == gridSize - 2 ? max : start + targetSize - 1;  // 마지막 파티션 범위 조정
            }

            log.info("{}", result);

            return result;
        }
    }

}

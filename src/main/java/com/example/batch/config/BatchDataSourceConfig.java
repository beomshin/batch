package com.example.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@EnableBatchProcessing(
        dataSourceRef = "batchDataSource",
        transactionManagerRef = "batchTransactionManager"
)
@Configuration

public class BatchDataSourceConfig {

    @Primary
    @Bean("batchDataSource")
    @ConfigurationProperties(prefix = "spring.batch-datasource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean("batchEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("batchDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.batch.entity.batch")
                .persistenceUnit("batchPersistenceUnit")
                .build();
    }

    @Primary
    @Bean(name = "batchTransactionManager")
    public PlatformTransactionManager batchTransactionManager(
            @Qualifier("batchEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(Objects.requireNonNull(entityManagerFactoryBean.getObject()));
        transactionManager.setNestedTransactionAllowed(true);
        return transactionManager;
    }

}

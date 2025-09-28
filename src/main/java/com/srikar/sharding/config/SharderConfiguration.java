package com.srikar.sharding.config;

import com.srikar.sharding.datasource.ShardedDataSourceRouter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

/**
 * Autoconfiguration for Sharder library.
 */
@Slf4j
@SpringBootConfiguration
@EnableConfigurationProperties(SharderProperties.class)
@EnableTransactionManagement
@ComponentScan(basePackages = "com.srikar.sharding")
public class SharderConfiguration {

    private final SharderProperties properties;

    public SharderConfiguration(SharderProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        ShardedDataSourceRouter router = new ShardedDataSourceRouter();
        HashMap<Object, Object> map = new HashMap<>();
        properties.datasource().forEach(ds -> {
            final HikariConfig shardedHikariConfig = hikariConfig();
            shardedHikariConfig.setJdbcUrl(ds.url());
            shardedHikariConfig.setUsername(ds.username());
            shardedHikariConfig.setPassword(ds.password());
            shardedHikariConfig.setDriverClassName(ds.driverClassName());
            if (StringUtils.isNotBlank(ds.schema())) {
                shardedHikariConfig.setSchema(ds.schema());
            }
            final HikariDataSource dataSource = new HikariDataSource(shardedHikariConfig);
            map.put(ds.key(), dataSource);
        });
        router.setTargetDataSources(map);
        router.setDefaultTargetDataSource(map.get(properties.defaultKey()));
        return router;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource())
                .packages("com.srikar")
                .build();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }

}
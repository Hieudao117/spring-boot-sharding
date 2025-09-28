package com.srikar.sharder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;


@ConfigurationProperties(prefix = "sharder")
public record SharderProperties(

        String defaultKey,
        List<ShardDataSource>datasource,

        Map<String, String> keyMapping,

        Map<String, String> rangeMapping
) {
    public enum ShardType {KEY, RANGE}

    public record ShardDataSource(
            String key,
            String url,
            String username,
            String password,
            String driverClassName,
            String jndi,
            String schema
    ) {
    }
}
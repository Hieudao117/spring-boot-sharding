package com.srikar.sharder.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routing datasource that selects the current target based on ShardContextHolder.
 */
@Slf4j
public class ShardedDataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = ShardContextHolder.getKey();
        log.debug("Current shard key: {}", key);
        return key;
    }
}


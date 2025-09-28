package com.srikar.sharding.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Routing datasource that selects the current target based on ShardContextHolder.
 */
@Slf4j
public class ShardedDataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String key = ShardContext.getKey();
        log.debug("Current shard key: {}", key);
        return key;
    }
}


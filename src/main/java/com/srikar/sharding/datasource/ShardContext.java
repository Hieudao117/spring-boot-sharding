package com.srikar.sharding.datasource;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local holder for current shard key.
 */
@Slf4j
public class ShardContext implements AutoCloseable {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public void set(String key) {
        log.info("Setting shard key: {}", key);
        CONTEXT.set(key);
    }

    public static String getKey() {
        return CONTEXT.get();
    }

    @Override
    public void close() throws Exception {
        log.info("Clearing shard key: {}", CONTEXT.get());
        CONTEXT.remove();
    }
}


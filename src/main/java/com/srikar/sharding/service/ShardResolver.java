package com.srikar.sharding.service;

/**
 * Interface to resolve which shard to use for a given key.
 */
public interface ShardResolver {
    /**
     * Resolves the shard identifier based on a lookup key.
     *
     * @param lookupKey the key used to determine the shard
     * @return the shard identifier (datasource key)
     */
    String resolve(Object lookupKey);
}


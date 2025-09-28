package com.srikar.sharding.service;

import com.srikar.sharding.config.SharderProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Default implementation of ShardResolver supporting both KEY-based and RANGE-based sharding.
 */
@Slf4j
@Getter
@Component
public class ShardResolverImpl implements ShardResolver {

    private final Map<String, String> keyMapper;
    private final TreeMap<Long, RangeEntry> rangeMapper;

    public ShardResolverImpl(SharderProperties properties) {
        this.keyMapper = properties.keyMapping() != null ? properties.keyMapping() : new HashMap<>();
        this.rangeMapper = parseRangeMap(properties.rangeMapping());
    }

    @Override
    public String resolve(Object lookupKey) {
        if (lookupKey == null) return null;

        // 1. Key-based mapping
        String keyStr = String.valueOf(lookupKey);
        String shard = keyMapper.get(keyStr);
        if (shard != null) return shard;

        // 2. Range-based mapping
        return lookupRange(lookupKey);

    }

    private String lookupRange(Object val) {
        try {
            var longVal = Long.parseLong(String.valueOf(val));
            return Optional.ofNullable(rangeMapper.floorEntry(longVal))
                    .filter(entry -> longVal <= entry.getValue().max)
                    .map(entry -> entry.getValue().shard())
                    .orElse(null);
        } catch (NumberFormatException e) {
            log.warn("Invalid value for range mapping: {}", val);
            return null;
        }
    }


    private TreeMap<Long, RangeEntry> parseRangeMap(Map<String, String> configRange) {
        TreeMap<Long, RangeEntry> tree = new TreeMap<>();
        if (CollectionUtils.isEmpty(configRange)) return tree;

        configRange.forEach((rangeStr, shard) -> {
            String[] parts = rangeStr.split("-");
            if (parts.length == 2) {
                try {
                    long min = Long.parseLong(parts[0]);
                    long max = Long.parseLong(parts[1]);
                    tree.put(min, new RangeEntry(min, max, shard));
                } catch (NumberFormatException ignored) {
                    log.warn("Invalid range format: {}", rangeStr);
                }
            }
        });
        return tree;
    }

    private record RangeEntry(long min, long max, String shard) {
    }
}

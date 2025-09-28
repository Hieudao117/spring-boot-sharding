package com.srikar.sharding.annotation;

import com.srikar.sharding.datasource.ShardContext;
import com.srikar.sharding.service.ShardResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Aspect that applies sharding logic to methods annotated with @Sharded.
 */
@Slf4j
@Aspect
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Component("shardedAspect")
public class ShardAspect {

    private final ShardResolver shardResolver;

    @Around("@annotation(com.srikar.sharding.annotation.Sharded)")
    public Object aroundShardedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Sharded sharded = method.getAnnotation(Sharded.class);
        String shardKey = !sharded.value().isEmpty()
                ? sharded.value()
                : extractShardKey(method, joinPoint.getArgs());

        Object result;
        try (ShardContext contextHolder = new ShardContext()) {
            contextHolder.set(shardResolver.resolve(shardKey));
            log.info("Shard key applied: {}", ShardContext.getKey());
            result = joinPoint.proceed();
        }
        return result;
    }

    private String extractShardKey(Method method, Object[] args) {
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation a : annotations[i]) {
                if (a instanceof ShardKey) {
                    return String.valueOf(args[i]);
                }
            }
        }
        return null;
    }
}


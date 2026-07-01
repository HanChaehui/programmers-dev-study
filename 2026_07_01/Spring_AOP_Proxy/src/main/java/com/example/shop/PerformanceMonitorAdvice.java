package com.example.shop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

public class PerformanceMonitorAdvice implements MethodInterceptor{
    @Override
    public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
        long start_time = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            System.out.printf("[PERF] %s.%s : %dms\n", invocation.getThis().getClass().getSimpleName(), invocation.getMethod().getName(), (System.nanoTime() - start_time) / 1_000_000);
        }
    }
}

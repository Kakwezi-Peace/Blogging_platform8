package com.example.Blogging_platform2.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    @Autowired
    private MetricsLogger metricsLogger;

    @Around("execution(* com.example.blogging_platform.asyncController..*(..))")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            metricsLogger.logPerformanceMetrics(methodName, executionTime, true);
            logger.info("Method {} executed successfully in {} ms", methodName, executionTime);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            metricsLogger.logPerformanceMetrics(methodName, executionTime, false);
            logger.error("Method {} failed after {} ms: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.example.blogging_platform.asyncService..*(..))")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();

        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        metricsLogger.logPerformanceMetrics(methodName, executionTime, true);
        logger.info("Service method {} executed in {} ms", methodName, executionTime);

        return result;
    }
}

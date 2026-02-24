package com.example.Blogging_platform2.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.blogging_platform.asyncController..*(..))")
    public void logControllerMethodEntry(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        logger.info("Entering controller method: {} with arguments: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "execution(* com.example.blogging_platform.asyncController..*(..))", returning = "result")
    public void logControllerMethodExit(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        logger.info("Exiting controller method: {} with result type: {}",
                methodName, result != null ? result.getClass().getSimpleName() : "null");
    }

    @AfterThrowing(pointcut = "execution(* com.example.blogging_platform.asyncController..*(..))", throwing = "exception")
    public void logControllerMethodException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        logger.error("Exception in controller method: {} - {}", methodName, exception.getMessage(), exception);
    }
}

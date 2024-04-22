package com.prox.challenge.gcoder.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {
    @Around("@annotation(com.prox.challenge.gcoder.service.anotation.MeasureExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MonitorService.plusCount();
        try{
            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            MonitorService.plusSuccess(executionTime);
            return result;
        }catch (Exception e){
            MonitorService.plusError();
            throw e;
        }
    }
}

package com.xinbo.sports.apiend.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author: wells
 * @date: 2020/7/8
 * @description:
 */
@Aspect
@Component
public class FiledAspect {

    @Pointcut("@annotation(com.xinbo.sports.apiend.aop.annotation.FiledAnnotation)")
    public void annotationPointCut() {
    }

    // @Around("annotationPointCut()")
    public void around(ProceedingJoinPoint joinPoint) {
        var object = joinPoint.getArgs()[0];
        object = "encrype" + object;
    }
}

package com.xinbo.sports.payment.aop.aspect;

import com.xinbo.sports.payment.aop.annotation.CheckToken;
import com.xinbo.sports.payment.base.HeaderBase;
import com.xinbo.sports.service.io.dto.BaseParams;
import com.xinbo.sports.service.thread.ThreadHeaderLocalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * @author: David
 * @date: 05/03/2020
 * @description:
 */
@Aspect
@Configuration
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenAspect {
    public static final ThreadLocal<BaseParams.HeaderInfo> HEADER_INFO = ThreadHeaderLocalData.HEADER_INFO_THREAD_LOCAL;
    private final HeaderBase headerBase;

    @Pointcut("execution(* com.xinbo.sports.payment.controller..*(..))")
    public void executeService() {
        // do nothing here
    }

    @Before("executeService()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取UnCheckToken 注解   有此注解不强制验证，无则强制认证
        CheckToken annotation = method.getAnnotation(CheckToken.class);
        if (annotation != null) {
            HEADER_INFO.set(headerBase.getHeaderLocalData(true,"api"));
        }
    }

    @After("executeService()")
    public void after(JoinPoint joinPoint) {
        HEADER_INFO.remove();
    }
}


package com.xinbo.sports.plat.aop.annotation;

import java.lang.annotation.*;

/**
 * @author: wells
 * @date: 2020/8/18
 * @description: 三方平台拉取数据异常处理
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ThirdPlatException {
    //异常类型
    ExceptionEnum exceptionType();
}

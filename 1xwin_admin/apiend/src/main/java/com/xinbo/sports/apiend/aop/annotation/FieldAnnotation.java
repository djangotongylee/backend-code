package com.xinbo.sports.apiend.aop.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface FieldAnnotation {
    boolean createFlag();
}

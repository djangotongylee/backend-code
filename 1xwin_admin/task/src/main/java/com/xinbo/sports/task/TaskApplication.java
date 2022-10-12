package com.xinbo.sports.task;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@MapperScan(value = {"com.xinbo.sports.**"})
@SpringBootApplication(scanBasePackages = {"com.xinbo.sports.**"})
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class TaskApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(TaskApplication.class, args);
        } catch (Exception e) {
            log.error("===> {} main run 异常:", TaskApplication.class.getSimpleName(), e);
        }
    }

}

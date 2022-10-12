package com.xinbo.sports.backend;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@MapperScan(value = {"com.xinbo.sports.**"})
@SpringBootApplication(scanBasePackages = {"com.xinbo.sports.**"})
@EnableKnife4j
public class BackendApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(BackendApplication.class, args);
        } catch (Exception e) {
            log.error("===> {} main run 异常:", BackendApplication.class.getSimpleName(), e);
        }
    }

}

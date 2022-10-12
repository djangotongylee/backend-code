package com.xinbo.sports.apiend;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author admin
 */
@Slf4j
@MapperScan(value = {"com.xinbo.sports.**"})
@SpringBootApplication(scanBasePackages = {"com.xinbo.sports.**"})
@EnableScheduling
@EnableAsync
public class ApiendApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ApiendApplication.class, args);
        } catch (Exception e) {
            log.error("===> {} main run 异常:", ApiendApplication.class.getSimpleName(), e);
        }
    }

}

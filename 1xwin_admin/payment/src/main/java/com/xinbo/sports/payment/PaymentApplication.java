package com.xinbo.sports.payment;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author admin
 */
@Slf4j
@MapperScan(value = {"com.xinbo.sports.**"})
@SpringBootApplication(scanBasePackages = {"com.xinbo.sports.**"})
public class PaymentApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(PaymentApplication.class, args);
        } catch (Exception e) {
            log.error("===> {} main run 异常:", PaymentApplication.class.getSimpleName(), e);
        }
    }

}

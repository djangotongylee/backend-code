package com.xinbo.sports.netty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author admin
 */
@MapperScan(value = {"com.xinbo.**"})
@SpringBootApplication(scanBasePackages = {"com.xinbo.**"})
@EnableScheduling
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
    }

}

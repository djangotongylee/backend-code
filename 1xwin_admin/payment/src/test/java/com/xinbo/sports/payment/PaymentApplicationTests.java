package com.xinbo.sports.payment;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@Slf4j
@SpringBootTest
class PaymentApplicationTests {

    @Test
    void contextLoads() {
        String str = "101550I2020071505324989691510000http://xxx.com/notify2e8eee466368501d48af31d80d4c21477";
        log.info(DigestUtils.md5DigestAsHex(str.getBytes()));
    }

}

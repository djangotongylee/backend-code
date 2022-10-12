package com.xinbo.sports.netty;

import com.xinbo.sports.netty.service.base.MyJedisPubSub;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.JedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class NettyApplicationTests {
    @Resource
    private JedisUtil jedisUtil;

    @Test
    void contextLoads() {
        jedisUtil.subscribe(new MyJedisPubSub(), KeyConstant.SUBSCRIBE_DN);
    }

}
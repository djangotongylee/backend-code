package com.xinbo.sports.utils;

import com.xinbo.sports.apiend.ApiendApplication;
import com.xinbo.sports.service.base.NoticeBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.common.Constant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiendApplication.class)
class RedisUtilsTest {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private NoticeBase noticeBase;
    @Autowired
    private JedisUtil jedisUtil;
    private static final String CHANNEL = "REMIND_MESSAGE";

    @Test
    void setRedisTemplate() {
        redisUtils.set("redis_key", "test redis");
    }

    @Test
    @SneakyThrows
    void testChannel() {
        while (true) {
            Thread.sleep(5000);
            /**
             * {消息类型，金额，单号，到账时间} 充值，提现
             * {"action": "DN_SUCCESS","message": {"orderId ": 1923467891,"coin ": 50.00,"accountDate": 1602999123},"device ": "D "}
             * {"action": "WN_SUCCESS","message": {"orderId ": 1923467891,"coin ": 50.00,"accountDate": 1602999123},"device ": "D "}
             *
             * {消息类型} 维护信息。。。
             * {"action":"MAINTAIN_INFO","message":{"gameId":101,"status":"",gameName":"SBO Sports","startTime":1602999123,"endTime":1602999123},"device":"D"}
             * 系统公告
             * {"action":"SYSTEM_NOTIFICATION","message":{"title":"标题","content":"内容"},"device":"D"}
             * 站内信
             * {"action":"STATION_LETTER","message":{"title":"标题","content":"内容,"uid":""},"device":"D"}
             * 体育预告
             * {"action":"SPORTS_TRAILER","message":{"title":"标题","content":"内容"},"device":"D"}
             * 活动公告
             * {"action":"ACTIVITY_NOTIFICATION,"message":{"title":"标题","content":"内容"},"device":"D"}
             */
            System.out.println("写入信息..");
            var dn = "{\"action\": \"DN_SUCCESS\",\"message\": {\"orderId \": 1923467892,\"uid\": 16,\"coin\": 50.00,\"accountDate\": 1602999123},\"device \": \"D \"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, dn);

            var wn = "{\"action\": \"WN_SUCCESS\",\"message\": {\"orderId \": 1923467891,\"uid\": 16,\"coin\": 100.00,\"accountDate\": 1602999123},\"device \": \"D \"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, wn);

            var ma = "{\"action\":\"MAINTAIN_INFO\",\"message\":{\"gameId\":101,\"status\":1,\"gameName\":\"SBO Sports\",\"startTime\":1602999123,\"endTime\":1602999123},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, ma);

            var ma1 = "{\"action\":\"MAINTAIN_INFO\",\"message\":{\"gameId\":101,\"status\":0,\"gameName\":\"SBO Sports\",\"startTime\":1601999123,\"endTime\":1602999123},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, ma1);

            var sy = "{\"action\":\"SYSTEM_NOTIFICATION\",\"message\":{\"title\":\"系统公告标题\",\"content\":\"系统公告内容\"},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, sy);

            var st = "{\"action\":\"STATION_LETTER\",\"message\":{\"title\":\"站内信标题\",\"content\":\"站内信内容\",\"uid\": 16},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, st);

            var sp = "{\"action\":\"SPORTS_TRAILER\",\"message\":{\"title\":\"体育标题\",\"content\":\"体育公告内容\"},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, sp);

            var av = "{\"action\":\"ACTIVITY_NOTIFICATION\",\"message\":{\"title\":\"活动标题\",\"content\":\"活动内容\"},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, av);


            writeDepositWithdrawal();
        }
    }

    @Test
    @SneakyThrows
    void testMaintain() {
        while (true) {
            Thread.sleep(1000);
            var ma = "{\"action\":\"MAINTAIN_INFO\",\"message\":{\"gameId\":101,\"gameName\":\"SBO Sports\",\"startTime\":1602999123,\"endTime\":1602999123},\"device\":\"D\"}";
            jedisUtil.leftPush(KeyConstant.NOTIFICATION_QUEUE, ma);
        }
    }

    @Test
    @SneakyThrows
    void testConsumer() {
        // while (true) {
        //Thread.sleep(1000);
        var message = jedisUtil.rightBPop(KeyConstant.SUBSCRIBE_DN);
        System.out.println("读取信息.." + message);
        //}
        //  jedisUtil.subscribe(new MyJedisPubSub(), KeyConstant.SUBSCRIBE_DN);
    }


    @SneakyThrows
    void writeDepositWithdrawal() {
        noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_DN);
        noticeBase.writeDepositAndWithdrawalCount(Constant.PUSH_WN);
    }

}
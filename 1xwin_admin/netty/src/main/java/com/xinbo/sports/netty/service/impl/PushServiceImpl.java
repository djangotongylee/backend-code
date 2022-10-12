package com.xinbo.sports.netty.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.netty.netty.ChatHandler;
import com.xinbo.sports.netty.service.PushService;
import com.xinbo.sports.service.base.DepositOrWithdrawalBase;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.utils.JedisUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @Author : Wells
 * @Date : 2020/9/25 1:21 上午
 * @Description : XX
 **/
@Service
@Slf4j
public class PushServiceImpl implements PushService {
    private static final AtomicInteger WN_ID = new AtomicInteger();
    private static final AtomicInteger DN_ID = new AtomicInteger();
    private static final String WN_MSG = "推送->提款笔数";
    private static final String DN_MSG = "推送->充值笔数";
    private static final ScheduledExecutorService POOL_WN = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, WN_MSG + "_ID_" + WN_ID.incrementAndGet()));
    private static final ScheduledExecutorService POOL_DN = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, DN_MSG + "_ID_" + DN_ID.incrementAndGet()));


    @Resource
    private DepositOrWithdrawalBase depositOrWithdrawalBase;
    @Resource
    private JedisUtil jedisUtil;

    /**
     * 推送给指定用户
     *
     * @param userId :用户id
     * @param msg    :消息内容
     * @Return void
     **/
    @Override
    public void pushMsgToOne(String userId, String msg) {

    }

    /**
     * 推送队列消息
     */
    @Scheduled(fixedDelay = 1000)
    public void sendNotificationQueue() {
        var message = jedisUtil.rightBPop(KeyConstant.NOTIFICATION_QUEUE);
        var msg = message.get(1);
        var uidStr = parseObject(parseObject(msg).getString("message")).getString("uid");
        var channelMap = ChatHandler.getApiChannelMap();
        if (StringUtils.isNotEmpty(uidStr)) {
            var uidList = Arrays.asList(uidStr.split(","));
            channelMap.entrySet().stream().filter(x -> {
                var uid = x.getKey().split("_")[1];
                return uidList.contains(uid);
            }).forEach(x ->
                    x.getValue().writeAndFlush(new TextWebSocketFrame(msg))
            );
        } else {
            ChatHandler.getApiChannelMap().values().forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(msg)));
        }
        log.info("DEPOSIT_WITHDRAWAL_QUEUE读取信息.." + msg);
    }

    /**
     * 推送存款提款条数队列消息
     */
    @Scheduled(fixedDelay = 1000)
    public void sendDepositWithdrawalQueue() {
        var message = jedisUtil.rightBPop(KeyConstant.DEPOSIT_WITHDRAWAL_QUEUE);
        ChatHandler.getBackendChannelMap().values().forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(message.get(1))));
        log.info("DEPOSIT_WITHDRAWAL_QUEUE读取信息.." + message);
    }


    @Scheduled(fixedDelay = 30000)
    public void heartbeat() {
        var heartJson = new JSONObject();
        heartJson.put("action", "KEEP_ALIVE");
        ChatHandler.getBackendChannelMap().values().forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(heartJson.toJSONString())));
        ChatHandler.getApiChannelMap().values().forEach(channel -> channel.writeAndFlush(new TextWebSocketFrame(heartJson.toJSONString())));
    }

}


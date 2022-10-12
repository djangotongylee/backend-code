package com.xinbo.sports.netty.service.base;

import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.netty.netty.ChatHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import redis.clients.jedis.JedisPubSub;

import java.util.Optional;

/**
 * @Author : Wells
 * @Date : 2020-12-10 4:15 下午
 * @Description : xx
 */
public class MyJedisPubSub extends JedisPubSub {

    @Override
    /** JedisPubSub类是一个没有抽象方法的抽象类,里面方法都是一些空实现
     * 所以可以选择需要的方法覆盖,这儿使用的是SUBSCRIBE指令，所以覆盖了onMessage
     * 如果使用PSUBSCRIBE指令，则覆盖onPMessage方法
     * 当然也可以选择BinaryJedisPubSub,同样是抽象类，但方法参数为byte[]
     **/
    public void onMessage(String channel, String message) {
        System.out.println(Thread.currentThread().getName() + "-接收到消息:channel=" + channel + ",message=" + message);
        var apiChannel = ChatHandler.getApiChannelMap().get(message);
        var reJson = new JSONObject();
        reJson.put("data", channel);
        Optional.ofNullable(channel).ifPresent(x ->
                apiChannel.writeAndFlush(new TextWebSocketFrame(reJson.toJSONString()))
        );
        //接收到exit消息后退出
        if (message.equals("exit")) {
            System.exit(0);
        }
    }
}

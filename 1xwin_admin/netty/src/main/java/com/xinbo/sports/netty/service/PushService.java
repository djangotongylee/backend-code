package com.xinbo.sports.netty.service;

/**
 * @Author: Wells
 * @Date: 2020/9/25 1:19 上午
 * @Description: 发送消息
 **/
public interface PushService {
    /**
     * 推送给指定用户
     *
     * @param userId :用户id
     * @param msg    :消息内容
     * @Return void
     **/
    void pushMsgToOne(String userId, String msg);



}

package com.xinbo.sports.netty.service;


import com.xinbo.sports.netty.io.bo.DataContent;
import io.netty.channel.Channel;

/**
 * @author: David
 * @date: 17/09/2020
 */
public interface UserOperateService {
    /**
     * 登出日志
     *
     * @param data 入参
     */
    boolean addLoginLog(DataContent data, Channel channel);

    /**
     * 登出日志
     *
     * @param channel 入参
     */
    void addLogoutLog(Channel channel);

    /**
     * 推送在线人数
     */
    void sendOnlineMessage();
}

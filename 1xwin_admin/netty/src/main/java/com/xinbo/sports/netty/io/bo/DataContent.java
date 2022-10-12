package com.xinbo.sports.netty.io.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息类型
 *
 * @author admin
 */
@Data
public class DataContent implements Serializable {

    private static final long serialVersionUID = 8021381444738260454L;

    /**
     * 动作类型
     */
    private String action;
    /**
     * 用户TOKEN
     */
    private String bearer;
    /**
     * Device
     */
    private String device;
    /**
     * URL
     */
    private String url;
    /**
     * 扩展字段
     */
    private String extend;
    /**
     * 重试
     */
    private String retry;
}

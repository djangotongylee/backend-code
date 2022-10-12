package com.xinbo.sports.netty.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xinbo.sports.dao.generator.po.User;
import com.xinbo.sports.dao.generator.po.UserLoginLog;
import com.xinbo.sports.dao.generator.service.UserLoginLogService;
import com.xinbo.sports.dao.generator.service.UserService;
import com.xinbo.sports.netty.io.bo.DataContent;
import com.xinbo.sports.netty.netty.ChatHandler;
import com.xinbo.sports.netty.service.UserOperateService;
import com.xinbo.sports.service.cache.KeyConstant;
import com.xinbo.sports.service.cache.redis.AdminCache;
import com.xinbo.sports.service.cache.redis.ConfigCache;
import com.xinbo.sports.service.cache.redis.UserCache;
import com.xinbo.sports.service.cache.redis.UserChannelRelCache;
import com.xinbo.sports.service.common.Constant;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.JedisUtil;
import com.xinbo.sports.utils.JwtUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: David
 * @date: 17/09/2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserOperateServiceImpl implements UserOperateService {
    private final ConfigCache configCache;
    private final UserService userServiceImpl;
    private final UserLoginLogService userLoginLogServiceImpl;
    private final JwtUtils jwtUtils;
    private final UserChannelRelCache userChannelRelCache;
    private final JedisUtil jedisUtil;
    private final AdminCache adminCache;
    private final UserCache userCache;
    /***
     * 是否重连
     */
    private static final String IS_RETRY = "0";

    /**
     * 新增登录/登出日志
     */
    @Override
    public boolean addLoginLog(DataContent data, Channel channel) {
        log.info(" ========== 登录日志 START ========== ");

        if (StringUtils.isBlank(data.getBearer())) {
            log.info("token不能为空！");
            return false;
        }
        BaseEnum.JWT jwtProp = configCache.getJwtProp();

        jwtUtils.init(jwtProp.getSecret(), jwtProp.getExpired());
        JSONObject jsonObject = jwtUtils.parseToken(data.getBearer());
        if (jsonObject == null) {
            log.info("无效 token！");
            return false;
        }
        String username = jsonObject.getString("username");
        Integer uid = jsonObject.getInteger("id");
        String webRole = jsonObject.getString("webRole");
        var key = webRole + "_" + uid;
        AttributeKey<String> attributeKey = AttributeKey.valueOf("BackendUser");
        channel.attr(attributeKey).setIfAbsent(webRole + "_" + uid);
        if (Constant.ADMIN.equals(webRole)) {
            String adminToken = jedisUtil.hget(KeyConstant.ADMIN_TOKEN_HASH, uid.toString());
            //添加用户对应的channel
            var backendChannelMap = ChatHandler.getBackendChannelMap();
            //token是否有效
            if (!data.getBearer().equals(adminToken)) {
                log.info("backend token已失效！");
                return false;
            }
            if (data.getRetry().equals(IS_RETRY)) {
                userQuit(backendChannelMap, key);
            }
            backendChannelMap.put(key, channel);
        } else if (Constant.WEB.equals(webRole)) {
            String apiToken = jedisUtil.hget(KeyConstant.USER_TOKEN_HASH, uid.toString());
            var apiChannelMap = ChatHandler.getApiChannelMap();
            //token是否有效
            if (!data.getBearer().equals(apiToken)) {
                log.info("api token已失效！");
                return false;
            }
            if (data.getRetry().equals("0")) {
                userQuit(apiChannelMap, key);
            }
            apiChannelMap.put(key, channel);
            sendOnlineMessage();
            //插入登录日志
            log.info("插入登录日志..." + uid);
            String channelId = channel.id().asLongText();
            userChannelRelCache.setUidChannelRel(uid, channelId);
            UserLoginLog one = userLoginLogServiceImpl.getOne(new QueryWrapper<UserLoginLog>()
                    .eq("uid", uid)
                    .orderByDesc("created_at")
                    .last("limit 1"));
            if (Objects.isNull(one) || one.getCategory().equals(0)) {
                Integer now = (int) Instant.now().getEpochSecond();
                UserLoginLog userOperateLog = new UserLoginLog();
                userOperateLog.setUid(uid);
                userOperateLog.setUsername(username);
                userOperateLog.setCoin(userServiceImpl.getById(uid).getCoin());
                userOperateLog.setUrl(data.getUrl());
                userOperateLog.setDevice(data.getDevice());
                AttributeKey<String> arrKey = AttributeKey.valueOf("ip");
                String ip = channel.attr(arrKey).get();
                userOperateLog.setIp(ip);
                userOperateLog.setCategory(1);
                userOperateLog.setCreatedAt(now);
                userOperateLog.setUpdatedAt(now);
                userLoginLogServiceImpl.save(userOperateLog);
                log.info(" ========== 登录日志 END ========== ");
            }
        }
        return true;
    }

    /**
     * 登出日志
     *
     * @param channel channel
     */
    @Override
    public void addLogoutLog(Channel channel) {
        log.info(" ========== 登出日志 START ========== ");

        String channelId = channel.id().asLongText();
        log.info(" ========== channelId:{}", channelId);
        Integer uid = userChannelRelCache.getUidByChannelId(channelId);
        log.info(" ========== UID:{}", uid);
        if (uid == null) {
            return;
        }
        AttributeKey<String> attributeKey = AttributeKey.valueOf("BackendUser");
        var deviceKey = channel.attr(attributeKey).get();
        // 删除缓存映射关系
        var webRole = deviceKey.split("_")[0];
        if ("admin".equals(webRole)) {
            //删除用户对应的channel
            Optional.ofNullable(ChatHandler.getBackendChannelMap().get(deviceKey)).ifPresent(ChannelOutboundInvoker::close);
            ChatHandler.getBackendChannelMap().remove(deviceKey);
        } else if ("web".equals(webRole)) {
            userChannelRelCache.delUidChannelRel(channelId);
            Optional.ofNullable(ChatHandler.getApiChannelMap().get(deviceKey)).ifPresent(ChannelOutboundInvoker::close);
            ChatHandler.getApiChannelMap().remove(deviceKey);
            sendOnlineMessage();
        }
        User user = userServiceImpl.getById(uid);
        if (null == user) {
            return;
        }
        Integer now = (int) Instant.now().getEpochSecond();
        UserLoginLog userOperateLog = new UserLoginLog();
        userOperateLog.setUid(uid);
        userOperateLog.setUsername(user.getUsername());
        userOperateLog.setCoin(user.getCoin());
        userOperateLog.setCategory(0);
        AttributeKey<String> key = AttributeKey.valueOf("ip");
        String ip = channel.attr(key).get();
        userOperateLog.setIp(ip);
        userOperateLog.setCreatedAt(now);
        userOperateLog.setUpdatedAt(now);
        userLoginLogServiceImpl.save(userOperateLog);

        log.info(" ========== 登出日志 END ========== ");
    }

    /**
     * 用户退出推送
     *
     * @param channelMap 通道Map
     * @param key        主键ID
     */
    public void userQuit(ConcurrentMap<String, Channel> channelMap, String key) {
        Optional.ofNullable(channelMap.get(key)).ifPresent(ch -> {
            var reJson = new JSONObject();
            reJson.put("action", Constant.USER_QUIT);
            var msgJSON = new JSONObject();
            msgJSON.put("flag", Constant.LOGOUT);
            reJson.put("message", msgJSON);
            ch.writeAndFlush(new TextWebSocketFrame(reJson.toJSONString()));
        });
    }

    /**
     * 推送在线人数
     */
    public void sendOnlineMessage() {
        var backendChannelmap = ChatHandler.getBackendChannelMap();
        backendChannelmap.forEach((key, channel) -> {
            var uid = Integer.parseInt(key.split("_")[1]);
            //代理用户处理
            var agentId = adminCache.getAdminInfoById(uid).getAgentId();
            List<Integer> userList;
            if (agentId != 0) {
                //获取代理的下级uid
                var uidList = userCache.getSupUid6ListByUid(agentId);
                userList = userChannelRelCache.getSubordinateListChannelUids(uidList);
            } else {
                userList = userChannelRelCache.getChannelUids();
            }
            var reJson = new JSONObject();
            reJson.put("action", BaseEnum.MsgActionEnum.PUSH_ON);
            var msgJson = new JSONObject();
            msgJson.put("count", userList.size());
            msgJson.put("ids", StringUtils.join(userList, ","));
            reJson.put("message", msgJson);
            channel.writeAndFlush(new TextWebSocketFrame(reJson.toJSONString()));
        });
    }

    /**
     * 项目重启清空在线人数的缓存
     */
    @PostConstruct
    public void deleteRelUidChannel() {
        jedisUtil.del("REL_UID_CHANNEL");
    }
}

package com.xinbo.sports.netty.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xinbo.sports.netty.io.bo.DataContent;
import com.xinbo.sports.netty.service.UserOperateService;
import com.xinbo.sports.service.io.enums.BaseEnum;
import com.xinbo.sports.utils.SpringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.xinbo.sports.service.io.enums.BaseEnum.MsgActionEnum.KEEP_ALIVE;

/**
 * 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 *
 * @author admin
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Getter
    private static final ConcurrentMap<String, Channel> backendChannelMap = new ConcurrentHashMap<>();
    @Getter
    private static final ConcurrentMap<String, Channel> apiChannelMap = new ConcurrentHashMap<>();

    protected static final String OPERATE_BEAN = "userOperateServiceImpl";

    /**
     * Channel 读数据
     *
     * @param ctx ctx
     * @param msg msg
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info("channelRead0读取信息==" + msg.text());
    }

    /**
     * 当客户端连接服务端之后（打开连接） 获取客户端的channle，并且放到ChannelGroup中去进行管理
     *
     * @param ctx ctx
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        UserOperateService userOperateService = (UserOperateService) SpringUtils.getBean(OPERATE_BEAN);
        userOperateService.sendOnlineMessage();
        log.info("发起连接！");
    }

    /**
     * 客户端异常
     *
     * @param ctx ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info(" ========== handlerRemoved START ==========");
        // 添加退出日志
        UserOperateService userOperateService = (UserOperateService) SpringUtils.getBean(OPERATE_BEAN);
        userOperateService.addLogoutLog(ctx.channel());
        log.info(" ========== handlerRemoved END ==========");
    }

    /**
     * 异常处理
     *
     * @param ctx   ctx
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info(" ========== exceptionCaught START ========== {}", cause.getMessage());
        // 添加退出日志
        UserOperateService userOperateService = (UserOperateService) SpringUtils.getBean(OPERATE_BEAN);
        userOperateService.addLogoutLog(ctx.channel());
        // 发生异常之后关闭连接（关闭channel），随后从ChannelGroup中移除
        ctx.channel().close();
        log.info(" ========== exceptionCaught END   ========== ");
    }

    /**
     * 第一次连接验证token
     *
     * @param ctx channel
     * @param msg msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (Objects.nonNull(msg) && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            var dataContent = new DataContent();
            var uriArr = request.uri().split("\\?token");
            if (uriArr.length != 2) {
                return;
            }
            var paramArr = uriArr[1].split("&");
            if (paramArr.length != 3) {
                return;
            }
            var tokenArr = paramArr[0].split("=");
            var device = paramArr[1].split("=");
            var retryArr = paramArr[2].split("=");
            if ("device".equals(device[0]) && "retry".equals(retryArr[0])) {
                dataContent.setBearer(tokenArr[1]);
                dataContent.setDevice(device[1]);
                dataContent.setRetry(retryArr[1]);
                UserOperateService userOperateService = (UserOperateService) SpringUtils.getBean(OPERATE_BEAN);
                var flag = userOperateService.addLoginLog(dataContent, ctx.channel());
                if (flag) {
                    super.channelRead(ctx, msg);
                }
            }
        } else if (Objects.nonNull(msg) && msg instanceof TextWebSocketFrame) {
            String content = ((TextWebSocketFrame) msg).text();
            // 1. 获取客户端发来的消息
            DataContent dataContent = JSON.parseObject(content).toJavaObject(DataContent.class);
            var keepEnum = EnumUtils.getEnumIgnoreCase(BaseEnum.MsgActionEnum.class, dataContent.getAction());
            if (keepEnum.equals(KEEP_ALIVE)) {
                var reJson = new JSONObject();
                reJson.put("action", "KEEP_ALIVE");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(reJson.toJSONString()));
                super.channelRead(ctx, msg);
            }
        }
    }
}

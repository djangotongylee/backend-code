package com.xinbo.sports.netty.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;


/**
 * @author admin
 */
@Slf4j
public class WSServerInitialzer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // websocket 基于http协议，所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 对httpMessage进行聚合，聚合成FullHttpRequest或FullHttpResponse
        // 几乎在netty中的编程，都会使用到此handler
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        // ====================== 以上是用于支持http协议    ======================


        // ====================== 增加心跳支持 start    ======================
        // 针对客户端，如果在1分钟时没有向服务端发送读写心跳(ALL)，则主动断开
        // 如果是读空闲或者写空闲，不处理
        pipeline.addLast(new IdleStateHandler(200, 200, 60));
        // 自定义的空闲状态检测
        pipeline.addLast(new HeartBeatHandler());
        // ====================== 增加心跳支持 end    ======================

        // 获取header需要在http到websocket升级完成之前，升级后就取不到了
        pipeline.addLast(new HttpHeadersHandler());
        // 自定义的handler
        pipeline.addLast(new ChatHandler());
        // ====================== 以下是支持httpWebsocket ======================
        /*
          websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
          本handler会帮你处理一些繁重的复杂的事
          会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
          对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws", true, 10485760));
    }

    class HttpHeadersHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String ip = null;
            if (msg instanceof FullHttpRequest) {
                HttpHeaders headers = ((FullHttpRequest) msg).headers();
                ip = getIp(headers);
            }
            // 如果为空则使用 netty 默认获取的客户端 IP
            if (null == ip) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            log.info("=======  ip ========>{}", ip);
            AttributeKey<String> key = AttributeKey.valueOf("ip");
            ctx.channel().attr(key).setIfAbsent(ip);
            ctx.fireChannelRead(msg);
        }

        /**
         * 获取ip
         *
         * @param headers headers
         * @return IP
         */
        private String getIp(HttpHeaders headers) {
            String ip = headers.get("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = headers.get("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip)) {
                ip = headers.get("WL-Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip)) {
                ip = headers.get("X-Real-IP");
            }
            return ip;
        }
    }

}

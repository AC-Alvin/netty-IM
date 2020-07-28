package com.alvin.boot.netty.ws;


import com.alvin.boot.netty.pojo.SendMessage;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 处理消息内容
 *
 * @Description:
 * @Author: alvin
 * @Date: 2020/5/29 17:40
 */
public class WssChannelHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private Gson gson = new Gson();
    /**
     * 用于记录和管理所有客户端的channel
     */
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 用于记录 用户的id 对应的channelId
     */
    public static Map<String, ChannelId> _USER_CHANNEL_ID_MAP = Maps.newConcurrentMap();
    /**
     * 发送消息缓存
     * 可以考虑使用guava的缓存
     * 指定过期时间
     */
    public static Map<String, Map<Long, SendMessage>> _USER_MESSAGE_CACHE = Maps.newConcurrentMap();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel channel = null;
        SendMessage message = gson.fromJson(msg.text(), SendMessage.class);
        if (message.getMsgType() == SendMessage.MsgType.TO_USER) {
            // 用户的消息,缓存起来
            _USER_MESSAGE_CACHE.get(message.getTo()).put(message.getId(), message);
        }
        System.out.println("接收到了客户端的消息是:\t" + msg.text());
        TextWebSocketFrame text = new TextWebSocketFrame(LocalDateTime.now() + "\t收到消息->\t" + msg.text());
        ChannelId channelId = _USER_CHANNEL_ID_MAP.get(message.getTo());
        if (channelId != null && (channel = clients.find(channelId)) != null) {
            channel = clients.find(channelId);
        }
        channel.writeAndFlush(text)
                .addListener((future) -> {
                    System.err.println("推送消息结果:" + future.isSuccess());
                    System.err.println("推送消息结果:" + future.cause());
                });
    }

    /**
     * 客户端创建的时候触发，当客户端连接上服务端之后，就可以获取该channel，然后放到channelGroup中进行统一管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
        _USER_CHANNEL_ID_MAP.put(ctx.channel().id().asLongText(), ctx.channel().id());
        _USER_MESSAGE_CACHE.put(ctx.channel().id().asLongText(), Maps.newConcurrentMap());
        System.err.println("client connection short id is:\t" + ctx.channel().id().asShortText());
        clients.writeAndFlush(new TextWebSocketFrame(ctx.channel().id().asShortText() + "\t上线了"));
        ctx.executor().schedule(() -> {
            if (!isLogin(ctx)) {
                // 关闭没有登录的 socket
                ctx.close();
            }
        }, 10, TimeUnit.SECONDS);
    }

    /**
     * 客户端销毁的时候触发
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当handlerRemoved 被触发时候，channelGroup会自动移除对应的channel
        clients.remove(ctx.channel());
        _USER_CHANNEL_ID_MAP.remove(ctx.channel().id().asLongText());
        System.out.println("客户端断开，当前被移除的channel的短ID是\t" + ctx.channel().id().asShortText());
        clients.writeAndFlush(new TextWebSocketFrame(ctx.channel().id().asShortText() + "\t下线了..."));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.writeAndFlush(new TextWebSocketFrame(cause.getClass().getName()));
    }

    /**
     * 是否登录
     *
     * @return
     */
    public boolean isLogin(ChannelHandlerContext ctx) {

        return true;
    }
}

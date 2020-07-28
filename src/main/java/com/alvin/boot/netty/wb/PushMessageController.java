package com.alvin.boot.netty.wb;

import com.alvin.boot.netty.ws.WssChannelHandler;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 推送消息
 * @Author: alvin
 * @Date: 2020/5/29 23:53
 */
@RestController
public class PushMessageController {

    @GetMapping("pushText")
    public String pushText(String msg) {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            ChannelGroupFuture future = WssChannelHandler.clients.writeAndFlush(new TextWebSocketFrame(LocalDateTime.now() + "\t" + msg));
            Boolean success = future.isSuccess();
            System.err.println(success);
        } finally {
            lock.unlock();
        }
        return "OK";
    }

    @GetMapping("pushText/{socketId}")
    public String pushText(String msg, @PathVariable String socketId) {
        // 查询字符串id 对应的channelId对象
        ChannelId channelId = WssChannelHandler._USER_CHANNEL_ID_MAP.get(socketId);
        if (channelId == null) {
            return "socket 不存在!";
        }
        // 给对应的channel发送消息
        WssChannelHandler.clients.find(channelId).writeAndFlush(new TextWebSocketFrame(LocalDateTime.now() + "\t" + msg));
        return "OK";
    }

}

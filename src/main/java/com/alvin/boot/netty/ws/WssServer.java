package com.alvin.boot.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: alvin
 * @Date: 2020/5/29 17:38
 */
@Component
public class WssServer implements CommandLineRunner {

    @Value("${netty.port}")
    private Integer port;

    @Override
    public void run(String... args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap server = new ServerBootstrap();
        server.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                //添加自定义初始化处理器
                .childHandler(new WssServerInitializer())
                .bind(port);
        System.err.println("netty start finish " + port);
    }

}

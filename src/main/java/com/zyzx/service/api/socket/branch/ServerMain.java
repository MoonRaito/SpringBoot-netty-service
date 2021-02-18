package com.zyzx.service.api.socket.branch;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServerMain {

    @Autowired
    private ServerInitializer ServerInitializer;

    @PostConstruct
    public void serverStart() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(ServerInitializer);
            b.option(ChannelOption.SO_BACKLOG, 2048);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,120000); // 超时时间

            b.bind(1818).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}

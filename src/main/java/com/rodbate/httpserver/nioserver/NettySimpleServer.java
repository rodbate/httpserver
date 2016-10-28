package com.rodbate.httpserver.nioserver;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class NettySimpleServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioSocketChannel.class);


    public static void main(String[] args) throws InterruptedException {


        EventLoopGroup boss = new NioEventLoopGroup(1);

        EventLoopGroup worker = new NioEventLoopGroup(8);


        ServerBootstrap b = new ServerBootstrap();

        b.group(boss, worker).channel(io.netty.channel.socket.nio.NioServerSocketChannel.class);

        b.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(new SimpleChannelInboundHandler() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        LOGGER.info("===== read msg : " + ((ByteBuf)msg).toString(Charset.forName("utf-8")));
                        ctx.writeAndFlush(Unpooled.copiedBuffer("response".getBytes())).addListener(ChannelFutureListener.CLOSE);
                    }
                });
            }

        });

        b.bind(new InetSocketAddress(10000)).sync();

    }
}

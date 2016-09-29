package com.rodbate.httpserver.handler;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpRequestDecoder());

        pipeline.addLast(new HttpResponseEncoder());

        //pipeline.addLast(new HttpServerHandler());
        //pipeline.addLast(new BaseRequestDispatcher());
    }
}

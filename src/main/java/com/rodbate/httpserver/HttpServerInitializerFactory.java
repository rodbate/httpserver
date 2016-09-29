package com.rodbate.httpserver;


import com.rodbate.httpserver.dispatcher.DefaultRequestDispatcher;
import com.rodbate.httpserver.http.RBHttpRequestDecoder;
import com.rodbate.httpserver.http.RBHttpResponseEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;






public class HttpServerInitializerFactory extends ChannelInitializer<SocketChannel> {



    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new RBHttpRequestDecoder())
                .addLast(new RBHttpResponseEncoder())
                .addLast(new DefaultRequestDispatcher());


    }


}

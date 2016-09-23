package com.rodbate.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {


    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {


        if (msg instanceof DefaultHttpRequest) {

            HttpRequest request = (DefaultHttpRequest) msg;


            String uri = request.uri();

            ByteBuf content = Unpooled.wrappedBuffer(uri.getBytes());

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK, content);

            response.headers().add("Content-Type", "application/json");
            response.headers().add("Content-Length", content.readableBytes());

            ctx.channel().writeAndFlush(response);
        }

    }
}

package com.rodbate.httpserver.dispatcher;


import com.rodbate.httpserver.http.RBHttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 *
 * 基础请求分发器
 *
 */
public abstract class BaseRequestDispatcher extends AbstractRequestDispatcher{

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseRequestDispatcher.class);

    @Override
    public void init() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        LOGGER.info("The remote address is {}", socketAddress.getHostName());
        super.channelActive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        dispatch(ctx, msg);

    }


    /**
     * 子类分发处理逻辑
     *
     * @param ctx ChannelHandlerContext
     * @param msg  HttpObject
     * @throws Exception ex
     */
    protected abstract void dispatch(ChannelHandlerContext ctx, HttpObject msg) throws Exception;





    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.channel().close();
    }
}

package com.rodbate.httpserver.dispatcher;


import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;


/**
 *
 * 抽象请求分发器
 *
 */
public abstract class AbstractRequestDispatcher extends SimpleChannelInboundHandler<HttpObject> {


    public abstract void init();


}

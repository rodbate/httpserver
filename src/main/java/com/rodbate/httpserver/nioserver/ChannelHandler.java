package com.rodbate.httpserver.nioserver;


import java.nio.ByteBuffer;


public interface ChannelHandler {


    void read(NioSocketChannel nsc, ByteBuffer buffer) throws Exception;

}

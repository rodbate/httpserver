package com.rodbate.httpserver.nioserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static com.rodbate.httpserver.nioserver.ServerHandler.*;


public class NioServerSocketChannel {


    private static final Logger LOGGER = LoggerFactory.getLogger(NioServerSocketChannel.class);


    private ServerSocketChannel ch;


    private NioSelectorLoop loop;

    private final ChannelHandler channelHandler;


    public NioServerSocketChannel(ServerSocketChannel ch, NioSelectorLoop loop, ChannelHandler channelHandler) {
        this.ch = ch;
        this.loop = loop;
        this.channelHandler = channelHandler;
    }


    public ServerSocketChannel channel(){
        return ch;
    }


    public NioSelectorLoop eventLoop(){
        return loop;
    }

    public IOOperation ioOperation(){
        return new IOOperation();
    }


    public class IOOperation {


        public void accept(){

            try {

                SocketChannel socketChannel = ch.accept();

                LOGGER.info("======== >>>> accept socket channel [{}]", socketChannel);

                socketChannel.configureBlocking(false);

                initSocketChannel(socketChannel, channelHandler);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}

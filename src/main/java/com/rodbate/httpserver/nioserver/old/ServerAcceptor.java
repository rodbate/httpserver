package com.rodbate.httpserver.nioserver.old;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;


public class ServerAcceptor implements Runnable {


    private final static Logger LOGGER = LoggerFactory.getLogger(ServerAcceptor.class);


    private final ServerSocketChannel serverSocketChannel;

    
    private final Queue<Socket> acceptQueue;


    private ReaderChannel readerChannel;


    public ServerAcceptor(ServerSocketChannel serverSocketChannel, Queue<Socket> acceptQueue, ReaderChannel readerChannel) {
        this.serverSocketChannel = serverSocketChannel;
        this.acceptQueue = acceptQueue;
        this.readerChannel =readerChannel;
    }

    @Override
    public void run() {


        /**
         *
         * 循环从操作系统的accept 队列中取出已就绪的tcp连接
         *
         */
        for (;;){

            try {

                SocketChannel socketChannel = serverSocketChannel.accept();

                LOGGER.info("=======  >>>>  accept socket channel [{}]", socketChannel);

                acceptQueue.offer(new Socket(socketChannel, readerChannel));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    public Queue<Socket> getAcceptQueue() {
        return acceptQueue;
    }
}

package com.rodbate.httpserver.nioserver;


import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerHandler {



    public static List<NioSelectorLoop> bossGroup;


    public static List<NioSelectorLoop> workerGroup;


    public static final IOThreadFactory threadFactory = new IOThreadFactory();

    private static final AtomicInteger bossIdx = new AtomicInteger();

    private static final AtomicInteger workerIdx = new AtomicInteger();



    public static NioSocketChannel initSocketChannel(SocketChannel channel, ChannelHandler channelHandler){

        NioSelectorLoop loop = nextWorkerLoop();

        NioSocketChannel nioSocketChannel = new NioSocketChannel(channel, loop, channelHandler);


        loop.execute(() -> {
            try {
                channel.register(loop.selector(), SelectionKey.OP_READ, nioSocketChannel);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
                try {
                    channel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });


        Thread thread = loop.thread();

        if (!thread.isAlive()){
            thread.start();
        }


        return nioSocketChannel;
    }


    public static NioSelectorLoop nextBossLoop(){

        return bossGroup.get(getIndex(bossIdx, bossGroup.size()));
    }

    public static NioSelectorLoop nextWorkerLoop(){
        return workerGroup.get(getIndex(workerIdx, workerGroup.size()));
    }


    private static int getIndex(AtomicInteger ai, int size){

        int id = ai.getAndIncrement();

        if (id == (Integer.MAX_VALUE / size) * size){
            ai.compareAndSet(id + 1, 0);
            id = ai.getAndIncrement();
        }

        return Math.abs(id % size);
    }

}

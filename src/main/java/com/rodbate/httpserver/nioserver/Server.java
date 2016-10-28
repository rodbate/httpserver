package com.rodbate.httpserver.nioserver;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;


public class Server {



    private final List<NioSelectorLoop> bossGroup = new ArrayList<>();


    private final List<NioSelectorLoop> workerGroup = new ArrayList<>();


    private final IOThreadFactory threadFactory = ServerHandler.threadFactory;

    private ChannelHandler channelHandler;


    public Server group(int boss, int worker){


        for (int i = 0; i < boss; i++) {
            NioSelectorLoop loop = new NioSelectorLoop();
            loop.thread(threadFactory.newThread(loop));
            bossGroup.add(loop);
        }


        for (int i = 0; i < worker; i++) {
            NioSelectorLoop loop = new NioSelectorLoop();
            loop.thread(threadFactory.newThread(loop));
            workerGroup.add(loop);
        }

        ServerHandler.bossGroup = bossGroup;
        ServerHandler.workerGroup = workerGroup;

        return this;
    }

    public Server handle(ChannelHandler channelHandler){
        this.channelHandler = channelHandler;
        return this;
    }


    public void bind(String hostname, int port){

        try {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setReuseAddress(true);

            NioSelectorLoop boss = ServerHandler.nextBossLoop();


            NioServerSocketChannel nioServer = new NioServerSocketChannel(serverSocketChannel, boss, channelHandler);

            serverSocketChannel.register(boss.selector(), SelectionKey.OP_ACCEPT, nioServer);

            serverSocketChannel.bind(new InetSocketAddress(hostname, port));

            boss.thread().start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }






}

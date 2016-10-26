package com.rodbate.httpserver.nioserver;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {


    private String hostname;

    private int port;


    public Server(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
    }

    public void start(){

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setReuseAddress(true);

            serverSocketChannel.bind(new InetSocketAddress(hostname, port));


            new Thread(new ServerAcceptor(serverSocketChannel, new LinkedBlockingQueue<>(1024))).start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

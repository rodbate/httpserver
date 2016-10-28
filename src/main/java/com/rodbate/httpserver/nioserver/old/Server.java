package com.rodbate.httpserver.nioserver.old;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

public class Server {


    private String hostname;

    private int port;

    private ReaderChannel readerChannel;

    public Server(String hostname, int port, ReaderChannel readerChannel) {
        this.port = port;
        this.hostname = hostname;
        this.readerChannel = readerChannel;
    }

    public void start(){

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            //serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().setReuseAddress(true);

            serverSocketChannel.bind(new InetSocketAddress(hostname, port));

            ServerAcceptor acceptor = new ServerAcceptor(serverSocketChannel, new ArrayBlockingQueue<>(1024), readerChannel);

            SocketHandler handler = new SocketHandler(acceptor);

            new Thread(acceptor).start();

            new Thread(handler).start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

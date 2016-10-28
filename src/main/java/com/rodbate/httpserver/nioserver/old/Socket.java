package com.rodbate.httpserver.nioserver.old;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Socket {

    private final String socketId;

    private final SocketChannel socketChannel;

    public ReaderChannel readerChannel;

    private WriterChannel writerChannel;


    private static final AtomicInteger COUNT = new AtomicInteger(1);


    public Socket(SocketChannel socketChannel, ReaderChannel readerChannel) {
        this.socketId = getUniqueId();
        this.socketChannel = socketChannel;
        this.readerChannel = readerChannel;
    }


    public int read(ByteBuffer buffer) throws IOException {

        return socketChannel.read(buffer);

    }

    public int write(ByteBuffer buffer) throws IOException {

        return socketChannel.write(buffer);

    }


    public SelectionKey register(Selector selector, int ops, Object attachment) throws ClosedChannelException {
        return socketChannel.register(selector, ops, attachment);
    }

    public SelectableChannel configureBlocking(boolean blocking) throws IOException {
        return socketChannel.configureBlocking(blocking);
    }

    private String getUniqueId(){

        int limit = 1000;

        int id = COUNT.getAndIncrement();

        if (id == limit) {
            while (!COUNT.compareAndSet(id + 1, 1)){
                //no op
            }
            id = COUNT.getAndIncrement();
        }

        return System.currentTimeMillis() + String.format("%03d", id);
    }


    public String getSocketId() {
        return socketId;
    }

}

package com.rodbate.httpserver.nioserver;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;


import static java.nio.channels.SelectionKey.*;



public class SocketHandler implements Runnable {


    private final ServerAcceptor acceptor;

    //读操作selector
    private Selector readSelector;

    //写操作selector
    private Selector writeSelector;


    //default 16k
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 16);


    public SocketHandler(ServerAcceptor acceptor) {
        this.acceptor = acceptor;
        try {
            readSelector = Selector.open();
            writeSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {


        for (;;){



        }

    }


    private void execute() throws IOException {

        takeAndRegisterOpReadChannel();

        readFromSocketChannel();
    }


    /**
     *
     * 从acceptQueue 中取出已就绪的tcp连接 并注册读操作事件
     *
     */
    private void takeAndRegisterOpReadChannel() throws IOException {

        Queue<Socket> acceptQueue = acceptor.getAcceptQueue();

        Socket socket;

        while ((socket = acceptQueue.poll()) != null) {

            socket.configureBlocking(false);

            socket.register(readSelector, OP_READ, socket);

        }

    }


    private void readFromSocketChannel() throws IOException {

        int len = readSelector.selectNow();

        if (len > 0) {

            Iterator<SelectionKey> it = readSelector.selectedKeys().iterator();

            while (it.hasNext()) {

                SelectionKey key = it.next();

                if (key.isValid()){

                    Socket socket = (Socket) key.attachment();



                    //socket.read(null);
                    // TODO: 2016/10/26 0026 while read

                    int length = 0;

                    readBuffer.clear();

                    while ((length = socket.read(readBuffer)) != -1){

                        readBuffer.flip();

                        socket.readerChannel.fireRead(readBuffer);

                    }


                }


                it.remove();
            }

        }

    }

}

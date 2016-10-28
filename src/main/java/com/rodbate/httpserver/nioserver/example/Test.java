package com.rodbate.httpserver.nioserver.example;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Test {


    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();



        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, serverSocketChannel);

        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", 8899));



        new Thread(){
            @Override
            public void run() {

                for (;;){

                    try {
                        while (selector.isOpen() && selector.select() > 0) {

                            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                            while (it.hasNext()) {

                                SelectionKey key = it.next();

                                it.remove();

                                if (key.isValid()) {

                                    if (key.isAcceptable()) {

                                        ServerSocketChannel attachment = (ServerSocketChannel) key.attachment();

                                        try {
                                            SocketChannel socketChannel = attachment.accept();

                                            System.out.println("======= >>>> accept socket channel  " + socketChannel);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }

                                }

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


            }
        }.start();

    }
}

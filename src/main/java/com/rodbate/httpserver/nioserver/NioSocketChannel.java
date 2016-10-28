package com.rodbate.httpserver.nioserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;



public class NioSocketChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioSocketChannel.class);

    private SocketChannel ch;


    private NioSelectorLoop loop;


    private ChannelHandler handler;


    public NioSocketChannel(SocketChannel ch, NioSelectorLoop loop, ChannelHandler handler) {
        this.ch = ch;
        this.loop = loop;
        this.handler = handler;
    }

    public SocketChannel channel(){
        return ch;
    }


    public NioSelectorLoop eventLoop(){
        return loop;
    }

    public IOOperation ioOperation(){
        return new IOOperation();
    }


    public class IOOperation {

        public ByteBuffer buffer = ByteBuffer.allocate(1024 * 16);

        public void read(SelectionKey key){

            try {

                for(;;){

                    buffer.clear();
                    if(ch.read(buffer) <= 0)
                    {
                        int interestOps = key.interestOps();
                        key.interestOps(interestOps & ~SelectionKey.OP_READ);
                        break;
                    }
                    buffer.flip();

                    LOGGER.info("=======  accept :    " + new String(buffer.array(), 0, buffer.limit()));

                    handler.read(NioSocketChannel.this, buffer);
                }



            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void close(){

        try {
            ch.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void write(ByteBuffer buffer){

        buffer.flip();

        try {
            ch.write(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeAndFlush(ByteBuffer buffer) {

        write(buffer);

        close();
    }
}

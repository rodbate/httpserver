package com.rodbate.httpserver.nioserver;


import java.nio.ByteBuffer;

public interface ReaderChannel {


    void init();


    void fireRead(ByteBuffer buffer);


}

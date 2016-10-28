package com.rodbate.httpserver.nioserver.old;


import java.nio.ByteBuffer;

public interface ReaderChannel {


    //void init();


    void fireRead(ByteBuffer buffer);


}

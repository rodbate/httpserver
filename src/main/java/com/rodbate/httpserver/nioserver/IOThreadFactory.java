package com.rodbate.httpserver.nioserver;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;



public class IOThreadFactory implements ThreadFactory {

    private static final AtomicInteger COUNT = new AtomicInteger(1);


    @Override
    public Thread newThread(Runnable r) {

        Thread t = new Thread(r);

        t.setName("NIO-Selector-Loop-" + COUNT.getAndIncrement());

        return t;
    }


}

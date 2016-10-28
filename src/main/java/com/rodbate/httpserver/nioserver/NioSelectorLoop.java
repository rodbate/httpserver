package com.rodbate.httpserver.nioserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class NioSelectorLoop implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioSocketChannel.class);

    private Thread thread;

    private Selector selector;


    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(10240);


    private final AtomicBoolean wakeUp = new AtomicBoolean();


    private final static long timeout = 10;


    public NioSelectorLoop() {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread thread(){

        return thread;
    }

    public void thread(Thread thread){
        this.thread = thread;
    }

    public Selector selector(){

        return selector;
    }


    public void execute(Runnable command){

        try {

            taskQueue.put(command);

        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

    }


    private boolean hasTask(){
        return !taskQueue.isEmpty();
    }

    @Override
    public void run() {

        for (;;) {

            try {


                if (selector.selectNow() == 0) {

                    wakeUp.getAndSet(false);

                    for (;;) {

                        if (selector.selectNow() > 0) {
                            break;
                        }

                        if (hasTask() && wakeUp.compareAndSet(false, true)) {
                            selector.selectNow();
                            break;
                        }

                        int select = selector.select(timeout);

                        if (select != 0 || hasTask() || wakeUp.get()) {

                            break;
                        }

                    }


                    if (wakeUp.get()) {
                        selector.wakeup();
                    }

                }

                processSelectedKeys();

                if (hasTask()){
                    Runnable cmd = taskQueue.poll();

                    if (cmd != null) {

                        cmd.run();

                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    private void processSelectedKeys() {

        Iterator<SelectionKey> it = selector.selectedKeys().iterator();


        while (it.hasNext()) {

            SelectionKey key = it.next();

            it.remove();

            processSelectedKey(key);
        }

    }


    private void processSelectedKey(SelectionKey key) {

        if (!key.isValid()){
            return;
        }

        int readyOps = key.readyOps();

        //accept
        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {

            NioServerSocketChannel serverSocketChannel = (NioServerSocketChannel) key.attachment();

            serverSocketChannel.ioOperation().accept();


        }

        //read
        if ((readyOps & SelectionKey.OP_READ) != 0) {

            NioSocketChannel socketChannel = (NioSocketChannel) key.attachment();

            socketChannel.ioOperation().read(key);


        }


        //write
        if ((readyOps & SelectionKey.OP_WRITE) != 0) {


        }

    }


}

package com.rodbate.httpserver.upload;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;


public class FileDeleteListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileDeleteListener.class);

    // 30 min
    private static final long DEFAULT_DURATION = 60 * 30;


    private static final CopyOnWriteArraySet<Strategy> TODO_SET =
            new CopyOnWriteArraySet<>();


    private static final CopyOnWriteArraySet<Strategy> FAILURE_SET =
            new CopyOnWriteArraySet<>();


    private final static Runnable DELETE_TASK = () -> {

        while (true) {

            for (Strategy s : TODO_SET) {

                long current = System.currentTimeMillis() / 1000;

                if (s.startTime + s.duration <= current) {

                    if (!s.file.delete()) {
                        FAILURE_SET.add(s);
                        LOGGER.info("=============  fail to delete file {} ", s.file.getPath());
                    } else {
                        TODO_SET.remove(s);
                        LOGGER.info("=============  delete file {} successfully", s.file.getPath());
                    }

                }

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

    };


    static {

        Thread task = new Thread(DELETE_TASK);

        task.setName("Daemon Thread Delete File");
        task.setDaemon(true);
        task.setPriority(Thread.NORM_PRIORITY);

        task.start();
    }



    public static void register(File file, long duration) throws IOException {

        if (!file.exists()) throw new IOException("File not exists");

        if (duration < 0) throw new IllegalArgumentException("Duration not negative");

        if (duration == 0) {
            duration = DEFAULT_DURATION;
        }

        Strategy strategy = new Strategy(file, System.currentTimeMillis() / 1000, duration);

        TODO_SET.add(strategy);
    }


    static class Strategy {

        File file;

        long startTime;

        long duration;

        public Strategy(File file, long startTime, long duration) {
            this.file = file;
            this.startTime = startTime;
            this.duration = duration;
        }
    }

}

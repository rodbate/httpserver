package com.rodbate.httpserver.upload;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

import static com.rodbate.httpserver.common.ServerConstants.*;


public class FileDeleteListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileDeleteListener.class);

    // 30 min
    private static final long DEFAULT_DURATION = 60 * 30;


    private static final File DELETE_ON_START = new File(JAVA_IO_TMPDIR, "httpserver_tmp_delete");


    private static final ReentrantLock LOCK = new ReentrantLock();


    private static final CopyOnWriteArraySet<Strategy> TODO_SET =
            new CopyOnWriteArraySet<>();


    private static final CopyOnWriteArraySet<Strategy> FAILURE_SET =
            new CopyOnWriteArraySet<>();


    private final static Runnable DELETE_TASK = () -> {

        while (true) {

            for (Strategy s : TODO_SET) {

                long current = System.currentTimeMillis() / 1000;

                if (s.startTime + s.duration <= current) {
                    if (!s.file.exists()) throw new RuntimeException("File not exists");
                    if (!s.file.delete()) {
                        TODO_SET.remove(s);
                        FAILURE_SET.add(s);
                        LOGGER.info("=============  fail to delete file {} ", s.file.getPath());
                    } else {
                        TODO_SET.remove(s);

                        LOGGER.info("=============  delete file {} successfully", s.file.getPath());

                        removeFilePath(s.file.getPath());
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


    private final static Runnable INIT_DELETE = () -> {


        try {
            if (DELETE_ON_START.exists()) {

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(new FileInputStream(DELETE_ON_START)));

                String line;

                while ((line = reader.readLine()) != null) {
                    if(new File(line).delete()){
                        LOGGER.info("=============  delete file {} successfully", line);
                    }
                }

                reader.close();

                if (DELETE_ON_START.delete()) {
                    LOGGER.info("=============  delete file {} successfully", DELETE_ON_START.getPath());
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
        }


    };


    public static void init(){
        Thread task = new Thread(INIT_DELETE);

        task.setName("Daemon Thread-1 Delete File");
        task.setDaemon(true);
        task.setPriority(Thread.NORM_PRIORITY);

        task.start();
    }


    static {

        Thread task = new Thread(DELETE_TASK);

        task.setName("Daemon Thread-2 Delete File");
        task.setDaemon(true);
        task.setPriority(Thread.NORM_PRIORITY);

        task.start();
    }


    public static void removeFilePath(String filePath){

        final ReentrantLock lock = LOCK;

        try {
            lock.lock();

            if (DELETE_ON_START.exists()) {

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(new FileInputStream(DELETE_ON_START)));

                String line;

                List<String> paths = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    if (!filePath.equals(line)) {
                        paths.add(line);
                    }
                }

                reader.close();

                FileOutputStream fos = new FileOutputStream(DELETE_ON_START);

                for (String s : paths) {

                    fos.write((s + LINE_SEPARATOR).getBytes());
                }

                fos.close();

            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }


    public static void register(File file, long duration) throws IOException {

        final ReentrantLock lock = LOCK;

        try {
            lock.lock();

            if (duration < 0) throw new IllegalArgumentException("Duration not negative");

            if (duration == 0) {
                duration = DEFAULT_DURATION;
            }

            Strategy strategy = new Strategy(file, System.currentTimeMillis() / 1000, duration);

            TODO_SET.add(strategy);

            if (!DELETE_ON_START.exists()) {
                DELETE_ON_START.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(DELETE_ON_START, true);
            fos.write((file.getPath() + LINE_SEPARATOR).getBytes());
            fos.close();
        } finally {
            lock.unlock();
        }
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

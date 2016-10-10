package com.rodbate.httpserver.test;



import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleDownloadClient {


    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    static volatile long sum = 0;

    public static long fileLength;


    //获取文件长度
    public static long getFileLength(String urlStr){
        HttpURLConnection connection = getConnection(urlStr);

        long length = 0;

        try {
            if (connection != null) {
                connection.setRequestMethod("GET");

                connection.connect();

                length = Long.valueOf(connection.getHeaderField("Content-Length"));

                fileLength = length;

                connection.disconnect();

            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return length;
    }

    //划分每个线程下载的文件长度
    public static Map<Integer, List<Long>> handleLengthPerThread(String urlStr){
        long fileLength = getFileLength(urlStr);

        Map<Integer, List<Long>> map = new HashMap<>();

        if (fileLength > 0) {

            //整除
            if (fileLength % PROCESSORS == 0){

                long unit = fileLength / PROCESSORS;

                for (int i = 1; i <= PROCESSORS; i++) {
                    List<Long> list = new ArrayList<>();
                    long start = (i - 1) * unit;
                    long end = i * unit - 1;
                    list.add(start);
                    list.add(end);
                    map.put(i, list);
                }
            }

            //不整除
            else {

                // 100/8=12  100%8=4  12*7=84  16
                long unit = fileLength / PROCESSORS;
                long remain = fileLength - unit * (PROCESSORS - 1);

                for (int i = 1; i < PROCESSORS; i++) {
                    List<Long> list = new ArrayList<>();
                    long start = (i - 1) * unit;
                    long end = i * unit - 1;
                    list.add(start);
                    list.add(end);
                    map.put(i, list);
                }

                List<Long> list = new ArrayList<>();
                long start = fileLength - remain;
                long end = fileLength - 1;
                list.add(start);
                list.add(end);

                map.put(PROCESSORS, list);

            }

        }

        return map;
    }

    public synchronized static void inc(long size) {
        sum += size;
    }

    public static HttpURLConnection getConnection(String urlStr) {

        try {
            URL url = new URL(urlStr);
            return (HttpURLConnection) url.openConnection();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static void main(String[] args) throws IOException {


        ExecutorService service = Executors.newFixedThreadPool(PROCESSORS);


        CountDownLatch signal = new CountDownLatch(PROCESSORS);




        String urlStr = "https://www.python.org/ftp/python/3.5.2/python-3.5.2.exe";

        Map<Integer, List<Long>> map = handleLengthPerThread(urlStr);

        File dir = new File("D:\\temp");

        if (!dir.exists()){
            dir.mkdirs();
        }

        for (int i = 1; i <= PROCESSORS; i++) {

            final int j = i;

            service.execute(() -> {

                HttpURLConnection connection = getConnection(urlStr);

                try {

                    if (connection != null) {
                        connection.setRequestMethod("GET");

                        connection.addRequestProperty("Range", String.format("bytes=%d-%d", map.get(j).get(0), map.get(j).get(1)));

                        connection.connect();

                        int responseCode = connection.getResponseCode();

                        if (responseCode == 200 || responseCode == 206){

                            InputStream is = connection.getInputStream();

                            File f = new File("D:\\temp\\temp_centos_" + j);

                            if (!f.exists()) {
                                f.createNewFile();
                            }

                            FileOutputStream fos = new FileOutputStream(f);

                            int len;
                            byte ba[] = new byte[1024];

                            while ((len = is.read(ba)) > 0) {
                                inc(len);
                                fos.write(ba, 0, len);
                            }

                            fos.close();
                            is.close();
                        }

                        connection.disconnect();
                        signal.countDown();
                    }


                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        DecimalFormat df = new DecimalFormat("####.00");

        new Thread(){
            @Override
            public void run() {
                while (true) {
                    double percent = Double.valueOf(df.format(Double.valueOf(sum * 1.0 / fileLength) * 100));
                    System.out.println("============== download percent ===== >>> : " + sum + "/" + fileLength + "     " + percent + "%");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //
        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        service.shutdown();

        //合并文件
        gatherTempFiles(dir, "temp_centos_", "python.exe");

    }

    public static int getIndex(File f) {
        return Integer.valueOf(f.getName().split("_")[2]);
    }

    public static void gatherTempFiles(File dir, String prefix, String target){

        if (dir.isDirectory()) {

            File file = new File(dir.getAbsolutePath() + File.separator + target);

            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);


                File[] files = dir.listFiles(pathname -> pathname.isFile() && pathname.getName().startsWith(prefix));


                //文件排序
                for (int i = 1; i < files.length; i++) {

                    int j = i;
                    File temp = files[i];
                    while (j > 0 && getIndex(files[j]) < getIndex(files[j - 1])){

                        files[j] = files[j - 1];
                        j--;
                    }

                    files[j] = temp;
                }


                for (int i = 0; i < files.length; i++) {

                    FileInputStream fis = new FileInputStream(files[i]);

                    int len;
                    byte ba[] = new byte[1024];

                    while ((len = fis.read(ba)) > 0) {

                        fos.write(ba, 0, len);
                    }

                    fis.close();

                    files[i].delete();

                }

                fos.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public void singleThread() throws IOException {
        URL url = new URL("http://127.0.0.1:8888/1.exe");

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");

        urlConnection.addRequestProperty("Range", "bytes=0-");

        urlConnection.connect();

        int responseCode = urlConnection.getResponseCode();

        long length = Long.valueOf(urlConnection.getHeaderField("Content-Length"));

        if (responseCode == 200 || responseCode == 206){

            InputStream is = urlConnection.getInputStream();

            File f = new File("D:\\1.exe");

            if (!f.exists()) {
                f.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(f);

            int sum = 0;
            int len;
            byte ba[] = new byte[1024];

            while ((len = is.read(ba)) > 0) {
                sum++;
                fos.write(ba, 0, len);

                //if (sum == length) break;
            }

            fos.close();
            is.close();
        }

        urlConnection.disconnect();
    }
}

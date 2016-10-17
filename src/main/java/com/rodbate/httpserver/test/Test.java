package com.rodbate.httpserver.test;



import com.rodbate.httpserver.upload.FileDeleteListener;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;

public class Test {


    public void get(Integer a){
        System.out.println(a + " ==== ");
    }

    public static void main(String[] args) throws Exception {

        /*CountDownLatch signal = new CountDownLatch(1000);

        long start = System.currentTimeMillis();

        URL url = new URL("http://127.0.0.1:8888/api/hello");

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");


        urlConnection.connect();

        int responseCode = urlConnection.getResponseCode();



        if (responseCode == 200) {

            InputStream is = urlConnection.getInputStream();

        }


        for (int i = 0; i < 50000; i++) {
            final int j =i;
            new Thread(){
                @Override
                public void run() {
                    try {
                        URL url = new URL("http://127.0.0.1:8888/api/hello");

                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                        urlConnection.setRequestMethod("GET");


                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();



                        if (responseCode == 200) {

                            InputStream is = urlConnection.getInputStream();

                            int len;
                            byte b[] = new byte[1024];
                            StringBuilder sb = new StringBuilder();
                            while ((len = is.read(b)) > 0) {

                                sb.append(new String(b, 0, len));
                            }

                            System.out.println("==================== " + j + "    response " + sb.toString());
                        }

                        signal.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }.start();
        }


        signal.await();

        System.out.println("   complete use time  ===== " + (System.currentTimeMillis() - start) / 1000 + "s");*/





    }
}

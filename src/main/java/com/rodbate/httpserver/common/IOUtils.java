package com.rodbate.httpserver.common;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class IOUtils {


    private static final int DEFAULT_BUFFER_SIZE = 1024;



    public static void copy(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];


        int len;

        while ((len = in.read(buffer)) != -1) {

            out.write(buffer, 0, len);
        }

    }


    public static void close(Closeable closeable) throws IOException {

        closeable.close();

    }
}

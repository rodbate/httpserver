package com.rodbate.httpserver.nioserver.example;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TestClient {



    public static void action() throws IOException {
        Socket socket = new Socket();

        socket.connect(new InetSocketAddress("127.0.0.1", 9999));

        OutputStream out = socket.getOutputStream();

        InputStream in = socket.getInputStream();

        out.write("ping server".getBytes());

        byte b[] = new byte[1024];

        int len = in.read(b);

        out.close();
        in.close();
        socket.close();

        System.out.println("===== response : \n" + new String(b, 0, len));
    }


    public static void main(String[] args) throws IOException {


        for (int i = 0; i < 100000; i++) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        action();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}

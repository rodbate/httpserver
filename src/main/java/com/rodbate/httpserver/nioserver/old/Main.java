package com.rodbate.httpserver.nioserver.old;






public class Main {


    public static void main(String[] args) {



        Server server = new Server("127.0.0.1", 8888, buffer -> {

            System.out.println("====== accept  : \n" + new String(buffer.array()));

        });

        server.start();

    }
}

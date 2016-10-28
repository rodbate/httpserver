package com.rodbate.httpserver.nioserver.example;


import com.rodbate.httpserver.nioserver.Server;

public class Main {


    public static void main(String[] args) {



        Server server = new Server();

        server.group(1,8)
            .handle(((nsc, buffer) -> {

                System.out.println("======== request : " + new String(buffer.array(), 0, buffer.limit()));

                String httpResponse = "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 38\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        "<html><body>Hello World!</body></html>";


                nsc.write(java.nio.ByteBuffer.wrap(httpResponse.getBytes()));



            })).bind("127.0.0.1", 8888);

    }
}

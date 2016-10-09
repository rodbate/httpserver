package com.rodbate.httpserver;

import com.rodbate.httpserver.common.RequestMappers;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;


import static com.rodbate.httpserver.common.StringUtil.*;
import static com.rodbate.httpserver.common.ServerConfig.*;
import static com.rodbate.httpserver.Version.*;

/**
 *
 * server 启动类
 *
 */
public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);


    public static void main() throws Exception {

        long start = System.currentTimeMillis();

        //boss 线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        initProperties();

        String portStr = getProperty("port");

        if (isNull(portStr)) throw new RuntimeException("port must not be null");

        int port = Integer.valueOf(portStr);

        String hostname = getProperty("hostname");

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.TCP_NODELAY, true);

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializerFactory());

            isBindPort(port);

            Channel channel;

            if (isNull(hostname)) {
                channel = bootstrap.bind(port).sync().channel();
            } else {
                channel = bootstrap.bind(hostname, port).sync().channel();
            }

            printLogo();

            RequestMappers.init();

            LOGGER.info("========>>>>>>>> Http Server Start Up ! Using Time [{}MS]", System.currentTimeMillis() - start);
            LOGGER.info("========>>>>>>>> Open your web browser and navigate to http://{}:{}", isNull(hostname) ? "127.0.0.1" : hostname, port);

            channel.closeFuture().sync();

        } catch (Exception e){
            //
            LOGGER.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


    public static void isBindPort(int port){

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);

        } catch (IOException e) {
            LOGGER.error("========>>>>>>>> Fail to bind to the port[{}]", port);
            LOGGER.error("========>>>>>>>> Http server exist");
            System.exit(1);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

    }



    private static void printLogo(){

        StringBuilder logo = new StringBuilder();


        InputStream is;

        BufferedReader br = null;

        String separator = System.getProperty("line.separator", "\n");

        logo.append(separator);

        try {

            is = ClassLoader.getSystemResourceAsStream("logo.txt");

            br = new BufferedReader(new InputStreamReader(is));

            String line;

            String line2;

            while ((line = br.readLine()) != null){
                logo.append(line);
                if ((line2 = br.readLine()) == null) {
                    logo.append("VERSION : ").append(VERSION).append(separator);
                } else {
                    logo.append(separator).append(line2).append(separator);
                }
            }


        } catch (FileNotFoundException e) {
            //ignore
            e.printStackTrace();
        } catch (IOException e) {
            //ignore
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        //System.out.println(logo.toString());
        LOGGER.info(logo.toString());
    }
}

package com.rodbate.httpserver;

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

/**
 *
 * server 启动类
 *
 */
public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static final int PORT = 8888;

    public static void main() {

        //boss 线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializerFactory());


            Channel channel = bootstrap.bind(PORT).sync().channel();

            printLogo();

            LOGGER.info("========>>>>>>>> Open your web browser and navigate to http://127.0.0.1:" + PORT);

            channel.closeFuture().sync();

        } catch (Exception e){
            //
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }





    private static void printLogo(){

        StringBuilder logo = new StringBuilder();

        InputStream is;

        BufferedReader br = null;

        String separator = System.getProperty("line.separator", "\n");

        try {

            is = Bootstrap.class.getResourceAsStream("logo.txt");

            br = new BufferedReader(new InputStreamReader(is));

            String line;

            while ((line = br.readLine()) != null){
                logo.append(line).append(separator);
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

        System.out.println(logo.toString());
    }
}

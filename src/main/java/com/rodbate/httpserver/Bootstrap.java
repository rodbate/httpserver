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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * server 启动类
 *
 */
public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private static int PORT;



    private final static Map<String, Object> properties = new HashMap<>();

    public static void main() {

        long start = System.currentTimeMillis();

        //boss 线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        initProperties();

        PORT = Integer.valueOf(getProperty("port"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.TCP_NODELAY, true);

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializerFactory());

            isBindPort(PORT);

            Channel channel = bootstrap.bind(PORT).sync().channel();

            printLogo();

            RequestMappers.init();

            LOGGER.info("========>>>>>>>> Http Server Start Up ! Using Time [{}MS]", System.currentTimeMillis() - start);
            LOGGER.info("========>>>>>>>> Open your web browser and navigate to http://127.0.0.1:" + PORT);

            channel.closeFuture().sync();

        } catch (Exception e){
            //
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


    public static void initProperties(){

        InputStream is = ClassLoader.getSystemResourceAsStream("httpserver.properties");

        Properties props = new Properties();

        try {
            props.load(is);

            Enumeration<?> names = props.propertyNames();

            while (names.hasMoreElements()) {

                String name = String.valueOf(names.nextElement());

                Object value = props.get(name);

                properties.put(name, value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getProperty(String name){
        return String.valueOf(properties.get(name));
    }


    private static void printLogo(){

        StringBuilder logo = new StringBuilder();

        InputStream is;

        BufferedReader br = null;

        String separator = System.getProperty("line.separator", "\n");

        try {

            is = ClassLoader.getSystemResourceAsStream("logo.txt");

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

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

        //boss 线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);

        //工作线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        initProperties();

        PORT = Integer.valueOf(getProperty("port"));

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);

            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializerFactory());


            Channel channel = bootstrap.bind(PORT).sync().channel();

            printLogo();

            RequestMappers.init();

            LOGGER.info("========>>>>>>>> Open your web browser and navigate to http://127.0.0.1:" + PORT);

            channel.closeFuture().sync();

        } catch (Exception e){
            //
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }


    public static void initProperties(){

        InputStream is = ClassLoader.getSystemResourceAsStream("server.properties");

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

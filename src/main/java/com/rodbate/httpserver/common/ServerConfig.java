package com.rodbate.httpserver.common;


import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServerConfig {


    public final static Map<String, Object> properties = new HashMap<>();


    public static void initProperties(){

        InputStream is = ClassLoader.getSystemResourceAsStream("httpserver.properties");

        if (is == null) throw new RuntimeException("未找到httpserver.properties");

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
        Object value = properties.get(name);
        return value == null ? null : String.valueOf(value);
    }


    public static final int HTTP_CACHE_SECONDS = 60;
}

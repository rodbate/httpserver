package com.rodbate.httpserver.test;



import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Test {


    public void get(Integer a){
        System.out.println(a + " ==== ");
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, UnknownHostException {


        String downloadPath = "/usr/local";

        if (downloadPath.endsWith(File.separator)) {
            downloadPath = downloadPath.substring(0, downloadPath.lastIndexOf(File.separator));
        }

        System.out.println(downloadPath);

    }
}

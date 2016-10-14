package com.rodbate.httpserver.test;


import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.io.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


import static com.rodbate.httpserver.common.HeaderNameValue.*;

@RequestMapping(value = "/api")
public class RequestTest {

    private static AtomicInteger COUNT = new AtomicInteger(1);

    @RequestMapping(value = "/hello", responseContentType = "text/html; charset=utf-8")
    public Object get(RBHttpRequest request, RBHttpResponse response){

        System.out.println("=============== invoke method times   " + COUNT.getAndIncrement());
        try {

            InputStream is = request.getFileItem().getInputStream();
            System.out.println("====== content body size  " + is.available());


            FileOutputStream out = new FileOutputStream("D:\\upload.txt");

            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1){
                out.write(b, 0, len);
            }
            is.close();
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

        return "success";
    }

    class Ret{
        private int type;
        private String desc;

        public Ret(int type, String desc) {
            this.type = type;
            this.desc = desc;
        }
    }

}

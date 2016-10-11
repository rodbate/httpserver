package com.rodbate.httpserver.test;


import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.io.*;
import java.util.Set;


import static com.rodbate.httpserver.common.HeaderNameValue.*;

@RequestMapping(value = "/api")
public class RequestTest {



    @RequestMapping(value = "/hello", responseContentType = "text/html; charset=utf-8")
    public Object get(RBHttpRequest request, RBHttpResponse response){



        /*Set<Cookie> cookie = request.getCookie();
        Cookie requestc = cookie.iterator().next();
        try {

            Cookie c = new DefaultCookie("test", "test");
            c.setPath("/");
            c.setDomain("127.0.0.1");
            c.setMaxAge(100000);
            //c.setSecure(true);
            Cookie c1 = new DefaultCookie("rodbate", "cookie");
            c1.setPath("/");
            c1.setDomain("127.0.0.1");
            c1.setMaxAge(100000);
            response.addCookie(c1);
            response.addCookie(c);

            System.out.println(" =============== cookie : === " + requestc.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Ret ret = new Ret(1, "ret");*/


        /*File f = new File("D:\\temp\\CenOS.zip");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //InputStream
        ByteArrayInputStream is = new ByteArrayInputStream("hhhhhhhhh".getBytes());

        response.setHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM);

        response.setFileName("CenOS.zip");*/

        //response.sendRedirect("http://www.baidu.com");

        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

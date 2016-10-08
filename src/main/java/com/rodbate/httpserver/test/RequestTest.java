package com.rodbate.httpserver.test;


import com.alibaba.fastjson.JSON;
import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.util.Set;

@RequestMapping(value = "/api")
public class RequestTest {



    @RequestMapping(value = "/hello", responseContentType = "text/html; charset=utf-8")
    public Object get(RBHttpRequest request, RBHttpResponse response){



        Set<Cookie> cookie = request.getCookie();
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

        return requestc.toString();
    }


}

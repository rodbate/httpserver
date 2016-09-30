package com.rodbate.httpserver.test;


import com.rodbate.httpserver.annotations.RequestMapping;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xpath.internal.operations.String;

@RequestMapping(value = "/api")
public class RequestTest {



    @RequestMapping(value = "/hello", method = {RequestMethod.GET, RequestMethod.POST}, responseContentType = "application/json")
    public Object get(RBHttpRequest request, RBHttpResponse response){

        System.out.println("=========== start " + response.headers().get("Content-Type"));

        response.setHeader("Content-Type", "text/html");

        System.out.println("=========== end " + response.headers().get("Content-Type"));

        return request.getParameter("a") + "  " + request.getParameter("b") + request.getJsonString();
    }


}

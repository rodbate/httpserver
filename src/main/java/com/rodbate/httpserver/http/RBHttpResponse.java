package com.rodbate.httpserver.http;


import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;


import static com.rodbate.httpserver.common.HeaderNameValue.*;

public class RBHttpResponse extends DefaultHttpResponse {


    public RBHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }


    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
        super(version, status, validateHeaders);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
    }


    public void setHeader(String name, Object value){
        headers().set(name, value);
    }

    public void setContentTypeIfAbsent(){
        if (headers().get(CONTENT_TYPE) == null) {
            headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        }
    }


    public void addCookie(Cookie cookie){
        headers().add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }


}

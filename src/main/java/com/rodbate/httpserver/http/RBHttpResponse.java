package com.rodbate.httpserver.http;


import com.rodbate.httpserver.common.StringUtil;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

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

    public void setHeaderIfAbsent(String name, Object value){

        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        if (headers().get(name) == null){
            headers().set(name, value);
        }
    }

    public void addCookie(Cookie cookie){
        headers().add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }


    //Content-Disposition: attachment; filename=xxx.txt
    public void setFileName(String fileName) {

        if (StringUtil.isNull(fileName)) throw new RuntimeException("filename must not be null");

        try {
            headers().set(CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName.trim(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendRedirect(String url){

        if (StringUtil.isNull(url)) throw new RuntimeException("url must not be null");

        setStatus(HttpResponseStatus.SEE_OTHER);

        headers().set(LOCATION, url);
    }
}

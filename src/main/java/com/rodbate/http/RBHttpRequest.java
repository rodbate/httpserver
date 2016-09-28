package com.rodbate.http;


import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class RBHttpRequest extends DefaultHttpRequest {


    public RBHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        super(httpVersion, method, uri);
    }

    public RBHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        super(httpVersion, method, uri, validateHeaders);
    }

    public RBHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, HttpHeaders headers) {
        super(httpVersion, method, uri, headers);
    }


}

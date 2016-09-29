package com.rodbate.httpserver.http;


import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

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

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers) {
        super(version, status, headers);
    }
}

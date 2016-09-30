package com.rodbate.httpserver.http;


import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

public class RBHttpResponse extends DefaultFullHttpResponse {


    public RBHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
        super(version, status, content);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
        super(version, status, validateHeaders);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders) {
        super(version, status, content, validateHeaders);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, content, validateHeaders, singleFieldHeaders);
    }

    public RBHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeaders) {
        super(version, status, content, headers, trailingHeaders);
    }


    public RBHttpResponse setContent(RBHttpResponse response, ByteBuf content){
        return new RBHttpResponse(
                                    response.protocolVersion(),
                                    response.status(),
                                    content,
                                    response.headers(),
                                    response.trailingHeaders());
    }

    public void setHeader(String name, Object value){
        headers().set(name, value);
    }

    public void setContentTypeIfAbsent(){
        if (headers().get("Content-Type") == null) {
            headers().set("Content-Type", "application/json; charset=UTF-8");
        }
    }



}

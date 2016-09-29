package com.rodbate.httpserver.http;


import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpVersion;


/**
 * http request 解码器
 *
 *
 */
public class RBHttpRequestDecoder extends HttpRequestDecoder {




    /**
     *
     * 将HttpRequest 转化为  RBHttpRequest
     *
     *
     * @param initialLine    GET  /uri  HTTP/1.1
     * @return RBHttpRequest
     * @throws Exception ex
     */
    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {


        //initialLine       GET     /uri    HTTP/1.1

        HttpMethod method = HttpMethod.valueOf(initialLine[0]);

        HttpVersion version = HttpVersion.valueOf(initialLine[2]);

        String uri = initialLine[1];

        return new RBHttpRequest(version, method, uri, validateHeaders);

    }


}

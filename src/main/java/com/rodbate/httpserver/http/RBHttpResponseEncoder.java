package com.rodbate.httpserver.http;


import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseEncoder;


import static com.rodbate.httpserver.common.HeaderNameValue.*;
import static com.rodbate.httpserver.common.NetUtil.*;

public class RBHttpResponseEncoder extends HttpResponseEncoder {


    @Override
    protected void encodeHeaders(HttpHeaders headers, ByteBuf buf) throws Exception {
        headers.add(SERVER, SERVER_VALUE);
        headers.add(HOST, getHttpHeaderHost());
        super.encodeHeaders(headers, buf);
    }
}

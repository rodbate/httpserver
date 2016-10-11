package com.rodbate.httpserver.http;


import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseEncoder;


import java.util.Date;

import static com.rodbate.httpserver.common.HeaderNameValue.*;
import static com.rodbate.httpserver.common.NetUtil.*;
import static com.rodbate.httpserver.common.ServerConstants.*;

public class RBHttpResponseEncoder extends HttpResponseEncoder {


    @Override
    protected void encodeHeaders(HttpHeaders headers, ByteBuf buf) throws Exception {
        headers.add(SERVER, SERVER_VALUE);
        headers.add(HOST, getHttpHeaderHost());

        if (headers.get(DATE) == null){
            headers.set(DATE, HTTP_SIMPLE_DATE_FORMATTER.format(new Date()));
        }

        super.encodeHeaders(headers, buf);
    }
}

package com.rodbate.httpserver.http;


import com.rodbate.httpserver.common.StringUtil;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


/**
 *
 * http request
 *
 */


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



    private final static HttpDataFactory FACTORY =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);


    private HttpPostRequestDecoder httpPostRequestDecoder;


    private Map<String, Object> params = new HashMap<>();

    private String jsonString;


    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public void setParameter(String key, Object value){
        params.put(key, value);
    }

    public Object getParameter(String key){
        return params.get(key);
    }



    public HttpPostRequestDecoder initParametersByPost() {

        HttpMethod method = method();

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {

            if (httpPostRequestDecoder == null) {
                httpPostRequestDecoder =
                        new HttpPostRequestDecoder(FACTORY, this, Charset.forName("UTF-8"));
            }

            try {

                //chunk
                List<InterfaceHttpData> bodyHttpDatas = httpPostRequestDecoder.getBodyHttpDatas();

                for (InterfaceHttpData bodyData : bodyHttpDatas) {
                    //attribute data
                    if (bodyData instanceof Attribute) {
                        Attribute attr = (Attribute) bodyData;
                        String name = attr.getName();
                        try {
                            String value = attr.getValue();

                            params.put(name, value);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                //if not chunk
                e.printStackTrace();
            }

        }


        return httpPostRequestDecoder;


    }



    public String getHeaderByName(String name){
        return headers().get(name);
    }


    /**
     * 获取cookie
     *
     * @return Set<Cookie>
     */
    public Set<Cookie> getCookie(){
        Set<Cookie> cookies;

        String value = headers().get("Cookie");

        if (StringUtil.isNull(value)) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }

        return cookies;
    }

}

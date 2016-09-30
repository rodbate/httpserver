package com.rodbate.httpserver.common;


import java.nio.charset.Charset;

public class ServerConstants {


    //默认编码
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");


    public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";


    public static final String MULTIPART_FORM_DATA = "multipart/form-data";


    public static final String TEXT_PLAIN = "text/plain";


    public static final String APPLICATION_JSON = "application/json";


    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

}

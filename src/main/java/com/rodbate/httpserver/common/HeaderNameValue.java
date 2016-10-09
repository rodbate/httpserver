package com.rodbate.httpserver.common;


import static com.rodbate.httpserver.Version.*;

public class HeaderNameValue {


    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_LENGTH = "Content-Length";

    public static final String SET_COOKIE = "Set-Cookie";

    public static final String COOKIE = "Cookie";

    public static final String CACHE_CONTROL = "Cache-Control";

    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    public static final String EXPIRES = "Expires";

    public static final String DATE = "Date";

    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    public static final String LAST_MODIFIED = "Last-Modified";

    public static final String REFERER = "Referer";

    public static final String SERVER = "Server";

    public static final String SERVER_VALUE = "Rodbate/Http Server[" + VERSION + "]";

    public static final String HOST = "Host";

    //redirect http重定向请求  （302, "Found")
    public static final String LOCATION = "Location";

    public static final String CONNECTION = "Connection";

    public static final String KEEP_ALIVE = "keep-alive";

    public static final String CLOSE = "close";

    public static final String ETAG = "ETag";

    public static final String IF_NONE_MATCH = "If-None-Match";

}

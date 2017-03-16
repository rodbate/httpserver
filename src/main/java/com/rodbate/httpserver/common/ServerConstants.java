package com.rodbate.httpserver.common;


import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ServerConstants {


    //默认编码
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");


    public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";


    public static final String MULTIPART_FORM_DATA = "multipart/form-data";


    public static final String TEXT_PLAIN = "text/plain";


    public static final String APPLICATION_JSON = "application/json";


    public static final String LINE_SEPARATOR = System.getProperty("line.separator");


    //Mon, 10 Oct 2016 06:20:27 GMT
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy hh:mm:ss zzz";


    public static final String HTTP_DATE_FORMAT_TIMEZONE = "GMT";


    public static final SimpleDateFormat HTTP_SIMPLE_DATE_FORMATTER =
            new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US) {
                private static final long serialVersionUID = -6267430985491785100L;

                {
                    super.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_FORMAT_TIMEZONE));
                }
            };



    public static final String DEFAULT_DOWNLOAD_PATH = System.getProperty("user.home");


    public static final String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");



    public static final String ISO_8859_1 = "ISO8859-1";


    public static final String HTTP_SEPARATOR = "\r\n";

    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {

        System.out.println(JAVA_IO_TMPDIR);

    }

}

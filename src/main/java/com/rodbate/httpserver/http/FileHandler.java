package com.rodbate.httpserver.http;


import com.sun.activation.registries.MimeTypeFile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.rodbate.httpserver.common.HeaderNameValue.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static com.rodbate.httpserver.common.ServerConstants.*;
import static com.rodbate.httpserver.common.StringUtil.*;
import static com.rodbate.httpserver.common.ServerConfig.*;


public class FileHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    /**
     *
     *
     * Request #1 Headers
     * ===================
     * GET /file1.txt HTTP/1.1
     * Range: bytes=0-999
     *
     * Response #1 Headers
     * ===================
     * HTTP/1.1 200 OK
     * Date:               Tue, 01 Mar 2016 22:44:26 GMT
     * Last-Modified:      Wed, 30 Jun 2015 21:36:48 GMT
     * Expires:            Tue, 01 Mar 2016 22:44:26 GMT
     * Cache-Control:      private, max-age=31536000
     * Content-Range: bytes 0-999/1000
     *
     * Request #2 Headers
     * ===================
     * GET /file1.txt HTTP/1.1
     * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
     *
     * Response #2 Headers
     * ===================
     * HTTP/1.1 304 Not Modified
     * Date:               Tue, 01 Mar 2011 22:44:28 GMT
     *
     */

    public static boolean download(ChannelHandlerContext ctx, RBHttpRequest request, String filePath){


        //判断目标文件是否存在
        File file = new File(filePath);

        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            return false;
        }

        // TODO: 2016/10/9 0009  ETag  If-None-Match

        String ifModifiedSince = request.getHeaderByName(IF_MODIFIED_SINCE);

        if (isNotNull(ifModifiedSince)) {

            SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);

            try {
                Date ifModifiedSinceDate = sdf.parse(ifModifiedSince);

                if (ifModifiedSinceDate.getTime() / 1000 == file.lastModified() / 1000){

                    sendNotModified(ctx);
                    return true;
                }

            } catch (ParseException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
            }

        }


        RandomAccessFile raf;

        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            sendError(ctx, NOT_FOUND);
            return false;
        }

        // TODO: 2016/10/9 0009  断点续传


        long fileLength;
        try {
            fileLength = raf.length();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            sendError(ctx, NOT_FOUND);
            return false;
        }


        /*String separator = System.getProperty("line.separator", "\n");
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            while ((line = raf.readLine()) != null){
                sb.append(line).append(separator);
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        //response.setContent(response, Unpooled.copiedBuffer(files));

        RBHttpResponse response = new RBHttpResponse(HttpVersion.HTTP_1_1, OK, Unpooled.copiedBuffer(sb.toString().getBytes()));
*/

        RBHttpResponse response = new RBHttpResponse(HttpVersion.HTTP_1_1, OK);

        response.setHeader(CONTENT_LENGTH, fileLength);

        if (HttpUtil.isKeepAlive(request)) {
            response.setHeader(CONNECTION, KEEP_ALIVE);
        }

        setContentTypeHeader(response, file);

        setDateAndCacheHeader(response, file);

        ctx.pipeline().addLast(new ChunkedWriteHandler());


        //先写出响应头
        ctx.channel().write(response);

        //写出响应体
        ChannelFuture sendFileFuture =
                ctx.channel().write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());

        ChannelFuture lastContentFuture =
                ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);





        return true;
    }

    private static void setDateAndCacheHeader(RBHttpResponse response, File file) {

        Calendar c = new GregorianCalendar();

        //date header Date: Tue, 01 Mar 2016 22:44:26 GMT
        response.setHeader(DATE, HTTP_SIMPLE_DATE_FORMATTER.format(c.getTime()));

        //cache header
        c.add(Calendar.SECOND, HTTP_CACHE_SECONDS);

        //Expires
        response.setHeader(EXPIRES, HTTP_SIMPLE_DATE_FORMATTER.format(c.getTime()));

        //cache-control
        response.setHeader(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);

        //last-modified
        response.setHeader(LAST_MODIFIED, HTTP_SIMPLE_DATE_FORMATTER.format(new Date(file.lastModified())));
    }

    private static void setContentTypeHeader(RBHttpResponse response, File file) {
        MimetypesFileTypeMap mineType = new MimetypesFileTypeMap();
        response.setHeader(CONTENT_TYPE, "application/octet-stream");
    }


    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {

        ByteBuf byteBuf = Unpooled.copiedBuffer(("Failure : " + status.toString()).getBytes());

        RBHttpResponse response = new RBHttpResponse(HttpVersion.HTTP_1_1, status, byteBuf);

        response.setHeader(CONTENT_TYPE, "text/plain; charset=utf-8");
        response.setHeader(CONTENT_LENGTH, byteBuf.readableBytes());
        response.setHeader(CONNECTION, CLOSE);


    }


    private static void sendNotModified(ChannelHandlerContext ctx) {

        RBHttpResponse response = new RBHttpResponse(HttpVersion.HTTP_1_1, NOT_MODIFIED);

        setDateHeader(response);

        response.setHeader(CONNECTION, CLOSE);
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setDateHeader(RBHttpResponse response) {
        response.setHeader(DATE, HTTP_SIMPLE_DATE_FORMATTER.format(new Date()));
    }


}

package com.rodbate.httpserver.http;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            return false;
        }



        long fileLength;
        try {
            fileLength = raf.length();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage(), e);
            return false;
        }

        // TODO: 2016/10/9 0009  断点续传
        //文件下载开始位置
        long startPos;

        //文件下载结束位置
        long endPos;

        //文件下载长度
        long transferLength;

        //Range: bytes=0-1000
        String requestRange = request.getHeaderByName(RANGE);

        RBHttpResponse response;

        if (isNotNull(requestRange)) {

            //Range: bytes=0-1000   Range: bytes=0-
            String str = requestRange.trim().split("=")[1];

            startPos = Long.valueOf(str.split("-")[0]);
            if (str.split("-").length == 2) {
                endPos = Long.valueOf(str.split("-")[1]);
                if (endPos + 1 > fileLength) {
                    endPos = fileLength - 1;
                }
            } else {
                endPos = fileLength - 1;
            }

            transferLength = endPos - startPos + 1;
            response = new RBHttpResponse(HttpVersion.HTTP_1_1, PARTIAL_CONTENT);

            response.setHeader(CONTENT_RANGE, String.format("bytes %d-%d/%d", startPos, endPos, transferLength));
        } else {
            startPos = 0;
            endPos = fileLength - 1;
            transferLength = fileLength;
            response = new RBHttpResponse(HttpVersion.HTTP_1_1, OK);
        }




        response.setHeader(CONTENT_LENGTH, transferLength);

        if (HttpUtil.isKeepAlive(request)) {
            response.setHeader(CONNECTION, KEEP_ALIVE);
        }

        response.setHeader(CONTENT_TYPE, APPLICATION_OCTET_STREAM);

        setDateAndCacheHeader(response, file);

        try {
            ctx.pipeline().addBefore("dispatcher", "chunkWriter", new ChunkedWriteHandler());
        } catch (IllegalArgumentException e) {
            LOGGER.info("========== .>>>>>>>  chunkWriter handler exists");
        }


        //先写出响应头
        ctx.channel().write(response);


        //写出响应体
       ChannelFuture sendFileFuture =
                ctx.channel().write(new DefaultFileRegion(raf.getChannel(), startPos, transferLength), ctx.newProgressivePromise());

        ChannelFuture lastContentFuture =
                ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);


        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                if (total < 0) {
                    LOGGER.info("{} Transfer progress: {}", future.channel(), progress);
                } else {
                    LOGGER.info("{} Transfer progress: {} / {}", future.channel(), progress, total);
                }

            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                LOGGER.info("{}  Transfer complete.", future.channel());
            }
        });


        if (!HttpUtil.isKeepAlive(request)) {

            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        //移除chunkWriter
        ctx.pipeline().remove("chunkWriter");

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




    private static void sendNotModified(ChannelHandlerContext ctx) {

        RBHttpResponse response = new RBHttpResponse(HttpVersion.HTTP_1_1, NOT_MODIFIED);

        response.setHeader(DATE, HTTP_SIMPLE_DATE_FORMATTER.format(new Date()));

        response.setHeader(CONNECTION, CLOSE);
        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }



}

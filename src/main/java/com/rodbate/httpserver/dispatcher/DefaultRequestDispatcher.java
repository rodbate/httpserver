package com.rodbate.httpserver.dispatcher;



import com.rodbate.httpserver.common.RequestMeta;
import com.rodbate.httpserver.http.FileHandler;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.rodbate.httpserver.common.RequestMappers.*;
import static com.rodbate.httpserver.common.ServerConstants.*;
import static com.rodbate.httpserver.common.StringUtil.*;
import static com.rodbate.httpserver.common.HeaderNameValue.*;
import static com.rodbate.httpserver.common.ServerConfig.*;



/**
 *
 * 默认分发器
 *
 *
 */
public class DefaultRequestDispatcher extends BaseRequestDispatcher {


    private HttpPostRequestDecoder decoder;

    private static final HttpDataFactory FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private static final Logger LOG = LoggerFactory.getLogger(DefaultRequestDispatcher.class);

    private RBHttpRequest request;

    private RequestMeta meta;

    @Override
    protected void dispatch(ChannelHandlerContext ctx, HttpObject msg) throws Exception {


        if (msg instanceof RBHttpRequest) {

            request = (RBHttpRequest) msg;

            String uri = request.uri();


            uri = URLDecoder.decode(uri, "UTF-8");



            //处理浏览器/favicon.ico请求
            if ("/favicon.ico".equals(uri)) {

                InputStream is = getClass().getResourceAsStream("/ico/httpserver.ico");

                byte[] bytes = new byte[is.available()];

                is.read(bytes);

                is.close();

                ByteBuf response = Unpooled.copiedBuffer(bytes);

                RBHttpResponse resp = new RBHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

                resp.setHeader(CONTENT_TYPE, "image/x-icon");
                resp.setHeader(CONTENT_LENGTH, bytes.length);
                //resp.setHeader(CONNECTION, CLOSE);

                ctx.channel().write(resp);
                ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            }

            //处理欢迎页面 "/"
            else if ("/".equals(uri) || "".equals(uri)){
                handlePageResponse(ctx, request, "/html/welcome.html", HttpResponseStatus.OK);
            }

            else {

                HttpMethod method = request.method();

                //处理文件下载
                if (method == HttpMethod.GET) {

                    String downloadPath = getProperty("downloadPath");

                    if (isNull(downloadPath)) {
                        downloadPath = DEFAULT_DOWNLOAD_PATH;
                    }

                    //去除路径最后文件路径分隔符   /usr/local/ -->/usr/local  C:\\a\\ --> C:\\a
                    if (downloadPath.endsWith(File.separator)) {
                        downloadPath = downloadPath.substring(0, downloadPath.lastIndexOf(File.separator));
                    }

                    String fileName = uri.replace("/", File.separator);

                    downloadPath = downloadPath + fileName;

                    LOG.info("===== ==============  download file path : "  + downloadPath);

                    boolean download = FileHandler.download(ctx, request, downloadPath);

                    if (download) {
                        return;
                    }

                }

                uri = parseUrl(uri, request);


                meta = getMeta(uri);

                //判断请求的URL是否正确
                if (meta == null) {

                    handlePageResponse(ctx, request, "/html/notFound.html", HttpResponseStatus.NOT_FOUND);

                } else {

                    List<RequestMethod> requestMethod = meta.getRequestMethod();

                    boolean methodAllow = false;

                    //处理请求方法为空 默认为ALL 的情况
                    if (requestMethod.size() == 1 && requestMethod.get(0).getDesc().equals("ALL")){
                        methodAllow = true;
                    }

                    else {
                        for (RequestMethod rm : requestMethod) {
                            if (method.name().equals(rm.getDesc())) {
                                methodAllow = true;
                                break;
                            }
                        }
                    }

                    if (methodAllow) {

                        //handle http post
                        if (request.method() == HttpMethod.POST || request.method() == HttpMethod.PUT) {

                            boolean readingChunk = HttpUtil.isTransferEncodingChunked(request);

                            LOG.info("=========== request chunk is {}  =======", readingChunk);

                            decoder = new HttpPostRequestDecoder(FACTORY, request, DEFAULT_CHARSET);

                        }

                        if (request.method() == HttpMethod.GET) {

                            dispatch0(ctx);

                        }
                    } else {
                        handlePageResponse(ctx, request, "/html/methodNotAllowed.html", HttpResponseStatus.METHOD_NOT_ALLOWED);
                    }
                }
            }

        }


        /*if (decoder != null) {


            if (msg instanceof HttpContent) {

                HttpContent content = (HttpContent) msg;

                decoder.offer(content);

                while (decoder.hasNext()) {

                    InterfaceHttpData data = decoder.next();

                    if (data != null) {

                        if (data instanceof Attribute) {

                            Attribute attr = (Attribute) data;

                            String name = attr.getName();
                            String value = attr.getValue();

                            request.setParameter(name, value);
                        }
                    }
                }

                dispatch0(ctx);
            }

        }*/


        if (request.method() != HttpMethod.GET && msg instanceof HttpContent) {

            try {

                handlePostBody(msg);

                dispatch0(ctx);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }


    }


    /**
     * 处理post请求体
     *
     * @param msg 请求体信息
     * @throws UnsupportedEncodingException ex
     */
    public void handlePostBody(HttpObject msg) throws UnsupportedEncodingException {

        HttpContent content = (HttpContent) msg;

        ByteBuf byteBuf = content.content();

        String rs = byteBuf.toString(DEFAULT_CHARSET);

        rs = URLDecoder.decode(rs, "UTF-8");

        String requestContentType = request.getHeaderByName(CONTENT_TYPE);

        LOG.info("================= request content type ======= {}", requestContentType);

        //a=12345&b=1111  Content-Type = application/x-www-form-urlencoded
        if (X_WWW_FORM_URLENCODED.equals(requestContentType)){


            LOG.info("================= post body ======= {}", rs);
            if (rs.contains("&")) {

                String[] split = rs.split("&");

                for (String s : split) {

                    if (s.contains("=")) {
                        String[] kv = s.split("=");
                        request.setParameter(kv[0], kv[1]);
                    }
                }
            }
        }


        /**
         *Content-Type = multipart/form-data; boundary=----WebKitFormBoundaryGzGFIXxIloOfAyym
         *
         ------WebKitFormBoundarynAeQKcYWF5Dz5XAt   0
         Content-Disposition: form-data; name="a"   1
         (空行)                                      2
         1111                                       3
         ------WebKitFormBoundarynAeQKcYWF5Dz5XAt   4
         Content-Disposition: form-data; name="b"   5
         (空行)                                      6
         dfsfsd                                     7
         ------WebKitFormBoundarynAeQKcYWF5Dz5XAt-- 8

         *
         *
         */

        if (isNotNull(requestContentType) && requestContentType.startsWith(MULTIPART_FORM_DATA)){


            LOG.info("================= post body ======= {}", rs);

            //去除空格
            requestContentType = removeBlankSpace(requestContentType);

            if (requestContentType.contains(";")) {

                //获取boundary
                String boundaryKv = requestContentType.split(";")[1];

                if (boundaryKv.contains("boundary")) {

                    String boundary = boundaryKv.split("=")[1];

                    //String endBoundary = boundary + "--";

                    List<String> lines = new LinkedList<>();

                    String[] split = rs.split(LINE_SEPARATOR);

                    lines.addAll(Arrays.asList(split));

                    int size = lines.size();

                    String name = "";
                    String value;

                    for (int i = 0; i < size; i++) {

                        //处理name
                        if (i % 4 == 1){
                            name = lines.get(i).split(";")[1].split("=")[1].replace("\"","");
                        }

                        //处理value
                        if (i % 4 == 3){
                            value = lines.get(i);
                            request.setParameter(name, value);
                        }


                    }

                }

            }

        }


        //text/plain; charset=UTF-8

        if (isNotNull(requestContentType) && requestContentType.startsWith(TEXT_PLAIN)) {

            LOG.info("================= post body ======= {}", rs);
            if (rs.contains("&")) {

                String[] split = rs.split("&");

                for (String s : split) {

                    if (s.contains("=")) {
                        String[] kv = s.split("=");
                        request.setParameter(kv[0], kv[1]);
                    }
                }
            }

        }


        //application/json; charset=UTF-8

        if (isNotNull(requestContentType) && requestContentType.startsWith(APPLICATION_JSON)) {

            request.setJsonString(rs);
        }
    }


    private void dispatch0(ChannelHandlerContext ctx) throws InstantiationException, IllegalAccessException {

        RBHttpResponse response = new RBHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

        Class target = meta.getTarget();

        Method m = meta.getMethod();

        response.setHeader(CONTENT_TYPE, meta.getResponseContentType());

        Object resp = invokeMethod(target, m, request, response);

        StringBuilder sb = new StringBuilder();
        sb.append(resp);

        ByteBuf buffer = Unpooled.copiedBuffer(sb.toString().getBytes());

        //response = response.setContent(response, buffer);

        response.setContentTypeIfAbsent();

        response.setHeader(CONTENT_LENGTH, buffer.readableBytes());


        // Connection: keep-alive
        if (HttpUtil.isKeepAlive(request)) {
            response.setHeader(CONNECTION, KEEP_ALIVE);
            //ctx.channel().writeAndFlush(response);
        }

        /*//  Connection: close
        else {
            response.setHeader(CONNECTION, CLOSE);
            ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }*/

        ctx.channel().write(response);
        ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
        /*if (!HttpUtil.isKeepAlive(request)){
            finish.addListener(ChannelFutureListener.CLOSE);
        }*/
    }


    /**
     *
     * 处理页面相应
     *
     * @param ctx ChannelHandlerContext
     * @param request 请求
     * @param pagePath 页面路径
     * @param status http response status
     * @throws IOException ex
     */
    public void handlePageResponse(
                                    ChannelHandlerContext ctx,
                                    RBHttpRequest request,
                                    String pagePath,
                                    HttpResponseStatus status) throws IOException {

        StringBuilder sb = new StringBuilder();

        InputStream is = getClass().getResourceAsStream(pagePath);

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        ByteBuf content = Unpooled.copiedBuffer(sb.toString().getBytes());

        RBHttpResponse resp = new RBHttpResponse(request.protocolVersion(), status);

        resp.setHeader(CONTENT_TYPE, "text/html");
        resp.setHeader(CONTENT_LENGTH, content.readableBytes());

        ctx.channel().write(resp);

        ctx.channel().writeAndFlush(content).addListener(ChannelFutureListener.CLOSE);

    }


    public Object invokeMethod(Class target, Method method, RBHttpRequest request, RBHttpResponse response) throws IllegalAccessException, InstantiationException {
        Object resp = null;

        Object instance = target.newInstance();

        boolean accessible = method.isAccessible();

        try {

            method.setAccessible(true);

            resp = method.invoke(instance, request, response);

        } catch (Exception e) {
            //e.printStackTrace();
            try {
                if (e instanceof IllegalArgumentException) {
                    resp = method.invoke(instance, request);
                }
            } catch (Exception e1) {
                //e1.printStackTrace();
                try {
                    if (e1 instanceof IllegalArgumentException) {
                        resp = method.invoke(instance, response);
                    }
                } catch (Exception e2) {
                    //e2.printStackTrace();
                    try {
                        if (e2 instanceof IllegalArgumentException) {
                            resp = method.invoke(instance);
                        }
                    } catch (Exception e3) {
                        //ignore
                        //e3.printStackTrace();
                    }
                }
            }

        } finally {
            method.setAccessible(accessible);
        }

        return resp;
    }

    private String parseUrl(String uri, RBHttpRequest request) {

        String url = uri;

        if (uri.contains("?")) {

            url = uri.substring(0, uri.indexOf("?"));

            String paramStr = uri.substring(uri.indexOf("?") + 1);

            String[] split = paramStr.split("&");

            for (String str : split) {
                if (str.contains("=")) {
                    String key = str.substring(0, str.indexOf("="));
                    String value = str.substring(str.indexOf("=") + 1);
                    request.setParameter(key, value);
                }
            }

        }

        return url;
    }


}

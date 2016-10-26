package com.rodbate.httpserver.dispatcher;



import com.rodbate.httpserver.common.RequestMeta;
import com.rodbate.httpserver.http.FileHandler;
import com.rodbate.httpserver.http.RBHttpRequest;
import com.rodbate.httpserver.http.RBHttpResponse;
import com.rodbate.httpserver.http.RequestMethod;
import com.rodbate.httpserver.upload.DiskFileItemFactory;
import com.rodbate.httpserver.upload.FileItem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

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

    private static final int SIZE_THRESHOLD = 10240;

    private static final long File_EXIST_DURATION = 10;

    private final static DiskFileItemFactory factory = new DiskFileItemFactory(SIZE_THRESHOLD, null, File_EXIST_DURATION);

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




        if (request.method() != HttpMethod.GET && msg instanceof HttpContent) {

            try {

                handlePostBody(msg);


            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        if (request.method() != HttpMethod.GET && msg instanceof LastHttpContent) {

            LOG.info(" =========== last http content : " + ((LastHttpContent) msg).content().toString());
            dispatch0(ctx);

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

        String rs = byteBuf.toString(Charset.forName(ISO_8859_1));


        String requestContentType = request.getHeaderByName(CONTENT_TYPE);


        //a=12345&b=1111  Content-Type = application/x-www-form-urlencoded
        if (X_WWW_FORM_URLENCODED.equals(requestContentType)){



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
         *Content-Type = multipart/form-data; boundary=--WebKitFormBoundarynAeQKcYWF5Dz5XAt
         *
         ----WebKitFormBoundarynAeQKcYWF5Dz5XAt     0     //--{bound}   start
         Content-Disposition: form-data; name="a"   1
         (空行)\r\n                                  2
         1111                                       3
         ----WebKitFormBoundarynAeQKcYWF5Dz5XAt     4
         Content-Disposition: form-data; name="b"   5
         (空行) \r\n                                 6
         dfsfsd                                     7
         ----WebKitFormBoundarynAeQKcYWF5Dz5XAt
         Content-Disposition: form-data; name="file"; filename="test.txt"  //上传文件
         Content-Type: application/octet-stream
         (空行)\r\n
         dfsfsd
         ----WebKitFormBoundarynAeQKcYWF5Dz5XAt--        //--{bound}--   end
         *
         *
         */

        if (isNotNull(requestContentType) && requestContentType.startsWith(MULTIPART_FORM_DATA)){


            //转化为DiskFile
            FileItem fileItem = request.getFileItem();

            if (fileItem == null) {
                fileItem = factory.createItem(null, null, "upload", false);
            }


            try {
                OutputStream outputStream = fileItem.getOutputStream();

                outputStream.write(rs.getBytes(ISO_8859_1));

                request.setFileItem(fileItem);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }



        //text/plain; charset=UTF-8

        if (isNotNull(requestContentType) && requestContentType.startsWith(TEXT_PLAIN)) {

            //LOG.info("================= post body ======= {}", rs);
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


    /**
     * Content-Disposition: form-data; name="file"; filename="test.txt"
     *
     * Content-Disposition: form-data; name="b"
     *
     * @param contentDisposition disposition
     * @return filename
     */
    private String getFilenameIfAbsent(String contentDisposition){

        Objects.requireNonNull(contentDisposition);

        String filename = null;

        if (contentDisposition.contains("filename")){
            filename = contentDisposition.split("=")[2].replace("\"", "");
        }

        return filename;
    }


    private void dispatch0(ChannelHandlerContext ctx) throws InstantiationException, IllegalAccessException {

        RBHttpResponse response = new RBHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

        Class target = meta.getTarget();

        Method m = meta.getMethod();

        response.setHeader(CONTENT_TYPE, meta.getResponseContentType());

        Object resp = invokeMethod(target, m, request, response);

        // Connection: keep-alive
        if (HttpUtil.isKeepAlive(request)) {
            response.setHeader(CONNECTION, KEEP_ALIVE);
        }


        //判断Content-Type : application/octet-stream
        if (APPLICATION_OCTET_STREAM.equalsIgnoreCase(response.headers().get(CONTENT_TYPE))){

            if (resp instanceof InputStream) {
                InputStream is = (InputStream) resp;

                byte b[] = new byte[4096];
                int len;
                //ctx.alloc().

                try {

                    response.setHeader(CONTENT_LENGTH, is.available());

                    ctx.channel().write(response);

                    while ((len = is.read(b)) > 0)
                    {
                        //sb.append(new String(b, 0, len));

                        ByteBuf buffer = Unpooled.copiedBuffer(new String(b, 0, len, ISO_8859_1).getBytes(ISO_8859_1));
                        ctx.channel().write(buffer);
                    }
                    ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (resp instanceof String) {

                String str = (String) resp;

                try {
                    ByteBuf buffer = Unpooled.copiedBuffer(str.getBytes(ISO_8859_1));

                    response.setHeader(CONTENT_LENGTH, buffer.readableBytes());

                    ctx.channel().write(response);
                    ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        } else {

            StringBuilder sb = new StringBuilder();
            sb.append(resp);

            ByteBuf buffer = Unpooled.copiedBuffer(sb.toString().getBytes());

            response.setContentTypeIfAbsent();

            response.setHeader(CONTENT_LENGTH, buffer.readableBytes());


            ctx.channel().write(response);
            ctx.channel().writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
        }

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

            try {
                if (e instanceof IllegalArgumentException) {
                    resp = method.invoke(instance, request);
                }
            } catch (Exception e1) {

                try {
                    if (e1 instanceof IllegalArgumentException) {
                        resp = method.invoke(instance, response);
                    }
                } catch (Exception e2) {

                    try {
                        if (e2 instanceof IllegalArgumentException) {
                            resp = method.invoke(instance);
                        }
                    } catch (Exception e3) {
                        //ignore

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

package com.rodbate.httpserver.upload;


import com.rodbate.httpserver.http.RBHttpRequest;

import java.io.*;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;



import static com.rodbate.httpserver.common.RequestMappers.*;
import static com.rodbate.httpserver.common.ServerConstants.*;
import static com.rodbate.httpserver.common.StringUtil.*;
import static com.rodbate.httpserver.common.HeaderNameValue.*;
import static com.rodbate.httpserver.common.ServerConfig.*;



/**
 *
 * multipart/form
 *
 * content body 解析器
 *
 *
 */
public class MultipartFileParser {


    private final FileItemFactory factory;


    public MultipartFileParser(FileItemFactory factory) {
        this.factory = factory;
    }



    /**
     *Content-Type = multipart/form-data; boundary=--WebKitFormBoundarynAeQKcYWF5Dz5XAt
     *
     ----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n     0     //--{bound}   start
     Content-Disposition: form-data; name="a"\r\n   1
     (空行)\r\n                                  2
     1111\r\n                                       3
     ----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n     4
     Content-Disposition: form-data; name="b"\r\n   5
     (空行) \r\n                                 6
     dfsfsd\r\n                                     7
     ----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n
     Content-Disposition: form-data; name="file"; filename="test.txt"\r\n  //上传文件
     Content-Type: application/octet-stream\r\n
     (空行)\r\n
     dfsfsd\r\n
     ----WebKitFormBoundarynAeQKcYWF5Dz5XAt--\r\n        //--{bound}--   end
     *
     *
     *
     * @param request request
     * @return list
     */
    public List<FileItem> parse(RBHttpRequest request){

        List<FileItem> fileItems = new ArrayList<>();

        String requestContentType = request.getHeaderByName(CONTENT_TYPE);

        requestContentType = removeBlankSpace(requestContentType);

        if (requestContentType.contains(";")) {

            //获取boundary
            String boundaryKv = requestContentType.split(";")[1];

            if (boundaryKv.contains("boundary")) {

                String boundary = boundaryKv.split("=")[1];

                String startBoundary = "--" + boundary;

                String endBoundary = "--" + boundary + "--";


                try {
                    FileItem multipartFile = request.getFileItem();

                    if (multipartFile != null) {

                        InputStream inputStream = multipartFile.getInputStream();

                        if (inputStream instanceof ByteArrayInputStream) {

                            ByteArrayInputStream bais = (ByteArrayInputStream) inputStream;

                            byte b[] = new byte[bais.available()];

                            bais.read(b);

                            bais.close();

                            String content = new String(b);

                            //结束标记
                            int flag = 0;

                            for (int i = 0; i < content.length();) {

                                if (flag == 1) {
                                    break;
                                }

                                //----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n  越过此行
                                i += startBoundary.length() + 2;

                                //Content-Disposition: form-data; name="a"\r\n
                                StringBuilder disposition = new StringBuilder();
                                while (true) {

                                    disposition.append(content.charAt(i++));

                                    if (content.charAt(i) == '\r' && content.charAt(i+1) == '\n'){

                                        i += 2;
                                        break;
                                    }
                                }

                                String dis = disposition.toString();

                                if (dis.contains("file")){

                                    String filename = dis.split(";")[2].split("=")[1].replace("\"", "");

                                    filename = URLDecoder.decode(filename, "utf-8");

                                    //获取content-type
                                    StringBuilder contentTypeSb = new StringBuilder();
                                    while (true) {

                                        contentTypeSb.append(content.charAt(i++));

                                        if (content.charAt(i) == '\r' && content.charAt(i+1) == '\n'){

                                            i += 2;
                                            break;
                                        }
                                    }

                                    String contentType = contentTypeSb.toString().split(":")[1].trim();

                                    //越过空行 \r\n
                                    i += 2;

                                    StringBuilder value = new StringBuilder();

                                    while (true) {

                                        value.append(content.charAt(i++));

                                        //下一个参数起始位置
                                        if (content.charAt(i) == '-' && content.charAt(i+1) == '-' &&
                                                content.substring(i, startBoundary.length() + 1).equals(startBoundary)) {

                                            String v = value.substring(0, value.length() - 1);

                                            FileItem item = factory.createItem("file", contentType, filename, false);
                                            OutputStream out = item.getOutputStream();

                                            out.write(v.getBytes());
                                            out.close();
                                            fileItems.add(item);
                                            break;
                                        }

                                        //结束
                                        if (content.charAt(i) == '-' && content.charAt(i+1) == '-' &&
                                                content.substring(i, endBoundary.length() + 1).equals(endBoundary)) {

                                            String v = value.substring(0, value.length() - 1);

                                            FileItem item = factory.createItem("file", contentType, filename, false);
                                            OutputStream out = item.getOutputStream();

                                            out.write(v.getBytes());
                                            out.close();
                                            fileItems.add(item);
                                            flag = 1;
                                            break;
                                        }
                                    }

                                } else {
                                    String name = dis.split(";")[1].split("=")[1].replace("\"", "");
                                    //越过空行 \r\n
                                    i += 2;

                                    StringBuilder value = new StringBuilder();

                                    while (true) {

                                        value.append(content.charAt(i++));

                                        //下一个参数起始位置
                                        if (content.charAt(i) == '-' && content.charAt(i+1) == '-' &&
                                                content.substring(i, startBoundary.length() + 1).equals(startBoundary)) {

                                            String v = value.substring(0, value.length() - 1);

                                            FileItem item = factory.createItem(name, null, null, true);
                                            OutputStream out = item.getOutputStream();

                                            out.write(v.getBytes());
                                            out.close();
                                            fileItems.add(item);
                                            break;
                                        }

                                        //结束
                                        if (content.charAt(i) == '-' && content.charAt(i+1) == '-' &&
                                                content.substring(i, endBoundary.length() + 1).equals(endBoundary)) {

                                            String v = value.substring(0, value.length() - 1);

                                            FileItem item = factory.createItem(name, null, null, true);
                                            OutputStream out = item.getOutputStream();

                                            out.write(v.getBytes());
                                            out.close();
                                            fileItems.add(item);
                                            flag = 1;
                                            break;
                                        }
                                    }
                                }


                            }
                        }


                        if (inputStream instanceof FileInputStream) {

                            FileInputStream fis = (FileInputStream) inputStream;

                            int length = fis.available();


                            FileChannel channel = fis.getChannel();

                            ByteBuffer buffer = ByteBuffer.allocate(1024);

                            ByteBuffer startBoundaryBuffer = ByteBuffer.allocate(startBoundary.length());

                            ByteBuffer endBoundaryBuffer = ByteBuffer.allocate(endBoundary.length());

                            ByteBuffer one = ByteBuffer.allocate(1);

                            long currentPosition = 0;

                            /**
                             *
                               ----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n
                               Content-Disposition: form-data; name="file"; filename="test.txt"\r\n
                               Content-Type: application/octet-stream\r\n
                               (空行)\r\n
                               dfsfsd\r\n
                             *
                             */

                            loop1:
                                while (true) {

                                    //buffer.clear();

                                    //----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n  越过此行
                                    channel.position(currentPosition += (startBoundary.length() + 2));

                                    //Content-Disposition: form-data; name="file"; filename="test.txt"\r\n
                                    channel.read(buffer);

                                    buffer.flip();

                                    //指针回退1024
                                    currentPosition = channel.position();
                                    currentPosition -= 1024;

                                    StringBuilder dispositionSb = new StringBuilder();
                                    for (;;) {

                                        byte b1 = buffer.get();
                                        ++currentPosition;
                                        if (b1 == '\r' && buffer.get() == '\n') {
                                            ++currentPosition;
                                            break;
                                        }
                                        dispositionSb.append((char)b1);
                                    }

                                    String disposition = dispositionSb.toString();

                                    if (disposition.contains("file")) {

                                        //file
                                        String filename = disposition.split(";")[2].split("=")[1].replace("\"", "");

                                        filename = URLDecoder.decode(filename, "utf-8");

                                        //Content-Type: application/octet-stream\r\n
                                        StringBuilder contentTypeSb = new StringBuilder();
                                        for (;;) {

                                            byte b1 = buffer.get();
                                            ++currentPosition;
                                            if (b1 == '\r' && buffer.get() == '\n') {
                                                ++currentPosition;
                                                break;
                                            }
                                            contentTypeSb.append((char)b1);
                                        }

                                        String contentTypeStr = contentTypeSb.toString();

                                        String contentType = contentTypeStr.split(":")[1].trim();

                                        // \r\n 越过此行
                                        //buffer.get();
                                        //buffer.get();

                                        currentPosition += 2;

                                        FileItem item = factory.createItem("file", contentType, filename, false);
                                        OutputStream outputStream = item.getOutputStream();

                                        //设置position
                                        channel.position(currentPosition);
                                        fileItems.add(item);


                                        for(;;){

                                            channel.read(one);

                                            channel.read(endBoundaryBuffer);
                                            currentPosition = channel.position();
                                            currentPosition -= endBoundary.length();
                                            channel.position(currentPosition);

                                            if (new String(endBoundaryBuffer.array()).equals(endBoundary)) {
                                                endBoundaryBuffer.clear();
                                                break loop1;
                                            }

                                            endBoundaryBuffer.clear();

                                            channel.read(startBoundaryBuffer);
                                            currentPosition = channel.position();
                                            currentPosition -= startBoundary.length();
                                            channel.position(currentPosition);



                                            if (new String(startBoundaryBuffer.array()).equals(startBoundary)) {
                                                startBoundaryBuffer.clear();
                                                buffer.clear();
                                                break;
                                            }

                                            startBoundaryBuffer.clear();

                                            outputStream.write(one.array());

                                            one.clear();
                                        }
                                        System.out.println();


                                    } else {

                                        //form field
                                        String name = disposition.split(";")[1].split("=")[1].replace("\"", "");
                                    }

                                }

                        }




                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        return fileItems;
    }
}

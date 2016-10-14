package com.rodbate.httpserver.upload;


import com.rodbate.httpserver.http.RBHttpRequest;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
                        }

                        if (inputStream instanceof FileInputStream) {
                            FileInputStream fis = (FileInputStream) inputStream;

                        }


                        /*for (int i = 0; i < rs.length();) {

                            //1, 判断起始位置  ----WebKitFormBoundarynAeQKcYWF5Dz5XAt\r\n
                            String startLine = rs.substring(i, startBoundary.length() + 1);

                            if (startBoundary.equals(startLine)){

                                //越过此行
                                i += startBoundary.length() + 2;

                                //2, 判断 Content-Disposition 取出此行
                                StringBuilder disposition = new StringBuilder();

                                while (true) {

                                    if (rs.charAt(i) == '\r' && rs.charAt(i+1) == '\n'){
                                        //此行结尾
                                        i += 2;
                                        //disposition.append("\r\n");
                                        break;
                                    }
                                    disposition.append(rs.charAt(i++));
                                }
                                *//**
                                * Content-Disposition: form-data; name="file"; filename="test.txt"
                                * Content-Disposition: form-data; name="a"
                                *//*


                                //上传文件类型
                                if (disposition.toString().contains("filename")){
                                    String filename =
                                            disposition.toString().split(";")[2].split("=")[1].replace("\"", "");

                                    request.setFileFlag(2);
                                }

                                //普通的参数类型
                                else {

                                    String name = disposition.toString().split(";")[1].split("=")[1].replace("\"", "");

                                    request.setFileFlag(1);
                                }

                                //去除空行 \r\n
                                i += 2;

                            }
                        }*/

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }



        return fileItems;
    }
}

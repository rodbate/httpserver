package com.rodbate.httpserver.test;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.*;

public class SimpleUploadClient {


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

    public static final String SEPARATOR = "\r\n";


    public static void main(String[] args) throws Exception {


        URL url = new URL("http://127.0.0.1:8888/api/hello");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");

        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        String boundary = "--SimpleUploadClient";

        conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.addRequestProperty("Connection", "keep-alive");
        conn.addRequestProperty("Charset", "utf-8");

        conn.connect();

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        FileInputStream fis = new FileInputStream(new File("D:\\完美世界.txt"));

        StringBuilder content = new StringBuilder();

        String filename = URLEncoder.encode("完美世界.txt", "utf-8");

        content.append("--").append(boundary).append(SEPARATOR);

        content.append("Content-Disposition: from/data; name=\"file\"; filename=\"" + filename + "\"").append(SEPARATOR);

        content.append("Content-Type: application/octet-stream").append(SEPARATOR).append(SEPARATOR);

        out.write(content.toString().getBytes());

        byte[] buffer = new byte[102400];

        int len;

        while ((len = fis.read(buffer)) != -1){
            out.write(buffer, 0, len);
        }

        out.write(SEPARATOR.getBytes());
        out.write(("--" + boundary + "--" + SEPARATOR).getBytes());
        out.flush();
        out.close();

        fis.close();

        int responseCode = conn.getResponseCode();

        System.out.println(responseCode);


        //conn.disconnect();
    }


}

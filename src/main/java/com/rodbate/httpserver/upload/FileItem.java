package com.rodbate.httpserver.upload;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileItem {


    InputStream getInputStream() throws IOException;


    String getContentType();


    String getFieldName();



    String getFileName();


    boolean isFormField();

    boolean isInMemory();



    byte[] get();


    String getString();


    long getSize();


    OutputStream getOutputStream() throws IOException;


}

package com.rodbate.httpserver.upload;


import com.rodbate.httpserver.common.IOUtils;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class DiskFileItem implements FileItem {

    private final static AtomicInteger COUNT = new AtomicInteger(0);

    private final String contentType;

    private final String fieldName;

    private final String filename;


    private final boolean isFormField;


    private final int sizeThreshold;


    private File repository;


    private byte[] cachedContent;

    private long size = -1;


    private DiskThresholdOutputStream outputStream;


    private File tempFile;


    public DiskFileItem(String contentType,
                        String fieldName,
                        String filename,
                        boolean isFormField,
                        int sizeThreshold,
                        File repository) {
        this.contentType = contentType;
        this.fieldName = fieldName;
        this.filename = filename;
        this.isFormField = isFormField;
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }


    @Override
    public InputStream getInputStream() throws IOException {
        if (!isInMemory()) {
            outputStream.close();
            return new FileInputStream(outputStream.getFile());
        }
        if (cachedContent == null) {
            cachedContent = outputStream.getData();
        }

        return new ByteArrayInputStream(cachedContent);
    }


    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }



    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public boolean isFormField() {
        return isFormField;
    }

    @Override
    public boolean isInMemory() {
        if (cachedContent != null) {
            return true;
        }

        return outputStream.isInMemory();
    }


    @Override
    public byte[] get() {

        if (isInMemory()){
            if (cachedContent == null){
                cachedContent = outputStream.getData();
            }
            return cachedContent;
        }

        byte array[] = new byte[(int)getSize()];

        InputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(outputStream.getFile()));
            is.read(array);
        } catch (IOException e) {
            array = null;
        } finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }

        return array;
    }

    @Override
    public String getString() {
        return new String(get());
    }

    @Override
    public long getSize() {
        if (size > 0) {
            return size;
        }

        if (isInMemory()) {
            if (cachedContent != null) {
                return cachedContent.length;
            } else {
                if (outputStream != null) {
                    cachedContent = outputStream.getData();
                    return cachedContent.length;
                }
            }
        } else {
            if (outputStream != null) {
                return outputStream.getFile().length();
            }
        }
        return 0;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            File tempFile = getTempFile();
            outputStream = new DiskThresholdOutputStream(sizeThreshold, tempFile);
        }
        return outputStream;
    }


    public File getTempFile() {

        if (tempFile == null) {
            File tmpDir = repository;
            if (repository == null){
                tmpDir = new File(System.getProperty("java.io.tmpdir"));
            }

            String tmpFilename = String.format("tmp_%s_%s", UUID.randomUUID().toString().replace("-", "_"), getUniqueId());

            tempFile = new File(tmpDir, tmpFilename);

            tempFile.deleteOnExit();
        }


        return tempFile;
    }


    private String getUniqueId() {

        int limit = 10000;

        int id = COUNT.getAndIncrement();

        if (id >= limit) {
            COUNT.compareAndSet(id + 1, 0);
            id = COUNT.getAndIncrement();
        }

        String idStr = String.valueOf(id);

        return ("0000" + idStr).substring(idStr.length());
    }



    public void writeToFile(File file) throws IOException {

        if (!file.exists()) throw new IOException("File not exists");

        if (isInMemory()){

            FileOutputStream fos = null;

            try{
                fos = new FileOutputStream(file);

                fos.write(get());

            } finally {
                if (fos != null) {
                    fos.close();
                }
            }

        } else {

            File storeFile = getStoreLocation();

            if (storeFile != null) {

                size = storeFile.length();

                if (!storeFile.renameTo(file)){

                    BufferedInputStream bis = null;

                    BufferedOutputStream bos = null;

                    try {
                        bis = new BufferedInputStream(new FileInputStream(storeFile));

                        bos = new BufferedOutputStream(new FileOutputStream(file));

                        IOUtils.copy(bis, bos);

                    } finally {

                        if (bis != null) {
                            bis.close();
                        }
                        if (bos != null) {
                            bos.close();
                        }
                    }

                }

            }

        }
    }



    public File getStoreLocation(){

        if (outputStream == null){
            return null;
        }

        return outputStream.getFile();
    }

}

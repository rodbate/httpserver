package com.rodbate.httpserver.upload;


import java.io.File;
import java.io.IOException;


public class DiskFileItemFactory implements FileItemFactory {


    private final static int DEFAULT_THRESHOLD = 10240;


    private int sizeThreshold = DEFAULT_THRESHOLD;

    private File repository;

    private long fileExistDuration;


    public DiskFileItemFactory() {
        this(DEFAULT_THRESHOLD, null, 0);
    }

    public DiskFileItemFactory(int sizeThreshold, File repository, long fileExistDuration) {
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
        this.fileExistDuration = fileExistDuration;
    }

    @Override
    public FileItem createItem(String fieldName,
                               String contentType,
                               String filename,
                               boolean isFormField) {


        DiskFileItem fileItem = new DiskFileItem(contentType,
                                                fieldName,
                                                filename,
                                                isFormField,
                                                sizeThreshold,
                                                repository);

        if (!isFormField) {
            try {
                FileDeleteListener.register(fileItem.getTempFile(), fileExistDuration);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileItem;
    }

    public int getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public File getRepository() {
        return repository;
    }

    public void setRepository(File repository) {
        this.repository = repository;
    }


    public long getFileExistDuration() {
        return fileExistDuration;
    }

    public void setFileExistDuration(long fileExistDuration) {
        this.fileExistDuration = fileExistDuration;
    }
}

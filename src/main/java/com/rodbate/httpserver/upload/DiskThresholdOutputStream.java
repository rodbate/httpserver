package com.rodbate.httpserver.upload;


import com.rodbate.httpserver.common.IOUtils;

import java.io.*;


public class DiskThresholdOutputStream extends ThresholdOutputStream {



    private OutputStream currentOutputStream;

    private ByteArrayOutputStream memoryOutputStream;

    private File outputFile;

    private final String prefix;

    private final String suffix;


    private final File directory;

    private boolean closed;


    public DiskThresholdOutputStream(int sizeThreshold,
                                     File outputFile,
                                     String prefix,
                                     String suffix,
                                     File directory) {

        super(sizeThreshold);
        this.outputFile = outputFile;
        this.prefix = prefix;
        this.suffix = suffix;
        this.directory = directory;
        memoryOutputStream = new ByteArrayOutputStream();
        currentOutputStream = memoryOutputStream;
    }


    public DiskThresholdOutputStream(int sizeThreshold, File outputFile){
        this(sizeThreshold, outputFile, null, null, null);
    }

    public DiskThresholdOutputStream(int sizeThreshold,
                                     String prefix,
                                     String suffix,
                                     File directory){
        this(sizeThreshold, null, prefix, suffix, directory);
        if (prefix == null) {
            throw new RuntimeException("Temporary File Prefix is Missing");
        }

    }


    @Override
    protected OutputStream getStream() {
        return currentOutputStream;
    }


    @Override
    protected void reachThreshold() throws IOException {
        if (prefix != null) {
            outputFile = File.createTempFile(prefix, suffix, directory);
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        memoryOutputStream.writeTo(fos);
        currentOutputStream = fos;
        memoryOutputStream = null;
    }


    public boolean isInMemory(){
        return !isThresholdExceeded();
    }

    public byte[] getData(){
        if (memoryOutputStream != null) {
            return memoryOutputStream.toByteArray();
        }
        return null;
    }

    public File getFile(){

        return outputFile;
    }

    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }


    public void writeTo(OutputStream outputStream) throws IOException {

        if (!closed) {
            throw new IOException("Stream not close");
        }

        if (isInMemory()){

            memoryOutputStream.writeTo(outputStream);

        } else {

            FileInputStream fis = null;

            try {

                fis = new FileInputStream(outputFile);

                IOUtils.copy(fis, outputStream);

            } catch (IOException e){
                //ignore
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        }
    }

}

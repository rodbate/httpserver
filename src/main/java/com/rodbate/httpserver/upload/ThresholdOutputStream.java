package com.rodbate.httpserver.upload;


import java.io.IOException;
import java.io.OutputStream;



public abstract class ThresholdOutputStream extends OutputStream {


    private int sizeThreshold;


    private long writtenIndex;


    private boolean thresholdExceeded;


    public ThresholdOutputStream(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }


    @Override
    public void write(int b) throws IOException {
        checkReachThreshold(1);
        getStream().write(b);
        writtenIndex++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }


    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkReachThreshold(len);
        getStream().write(b, off, len);
        writtenIndex += len;
    }

    @Override
    public void flush() throws IOException {
        getStream().flush();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        }catch (IOException e){
            //ignore
        }
        getStream().close();
    }

    public int getSizeThreshold() {
        return sizeThreshold;
    }

    public long getByteCount(){
        return this.writtenIndex;
    }

    public boolean isThresholdExceeded(){
        return writtenIndex > sizeThreshold;
    }

    protected void checkReachThreshold(int count) throws IOException {

        if (!thresholdExceeded && writtenIndex + count > sizeThreshold){
            thresholdExceeded = true;
            reachThreshold();
        }
    }

    public void resetByteCount(){
        thresholdExceeded = false;
        writtenIndex = 0;
    }



    protected abstract OutputStream getStream();



    protected abstract void reachThreshold() throws IOException;


}

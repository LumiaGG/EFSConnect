package com.jwg.efsconnect.FileShare;

import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends InputStream {

    private InputStream in;
    private long length, sumRead;
    private ProgressListener listener;

    public ProgressInputStream(InputStream inputStream, long length) {
        this.in = inputStream;
        sumRead = 0;
        this.length = length;
    }


    @Override
    public int read(byte[] b) throws IOException {
        int readCount = in.read(b);
        sumRead += readCount;
        notifyListener();
        return readCount;
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readCount = in.read(b, off, len);
        sumRead += readCount;
        notifyListener();
        return readCount;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = in.skip(n);
        sumRead += skip;
        notifyListener();
        return skip;
    }

    @Override
    public int read() throws IOException {
        int read = in.read();
        if (read != -1) {
            sumRead += 1;
            notifyListener();
        }
        return read;
    }

    public ProgressInputStream setListener(ProgressListener listener) {
        this.listener = listener;
        return this;
    }

    private void notifyListener() {
        listener.update(sumRead, length);
    }

}
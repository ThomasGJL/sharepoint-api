package com.kyndryl.sharepoint.util;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

public class CommonInputStreamResource extends InputStreamResource {

    private String fileName;
    private long fileLength;


    public CommonInputStreamResource(InputStream inputStream) {
        super(inputStream);
    }

    public CommonInputStreamResource(InputStream inputStream, String fileName, long fileLength) {
        super(inputStream);
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    @Override
    public long contentLength() {
        long estimate = fileLength;
        return estimate == 0 ? 1 : estimate;
    }
}

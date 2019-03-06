package com.darren.ca.server.payload;

import java.util.Arrays;

public class Response {
    private int responseCode;
    private byte[] fileData;

    public byte[] getResponseBytes() {
        return hasFile() ?
                (responseCode + Arrays.toString(fileData)).getBytes() :
                String.valueOf(responseCode).getBytes();
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    private boolean hasFile() {
        return fileData != null;
    }
}

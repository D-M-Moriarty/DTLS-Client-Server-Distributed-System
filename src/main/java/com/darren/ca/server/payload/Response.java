package com.darren.ca.server.payload;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Response {
    private int responseCode;
    private byte[] fileData;

    public byte[] getResponseBytes() {
        return hasFile() ?
                ArrayUtils.addAll(Integer.toString(responseCode).getBytes(), fileData) :
                String.valueOf(responseCode).getBytes();
    }

    private Response() {
    }

    @NotNull
    @Contract(" -> new")
    public static Response initializeResponse() {
        return new Response();
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Contract(pure = true)
    private boolean hasFile() {
        return fileData != null;
    }
}

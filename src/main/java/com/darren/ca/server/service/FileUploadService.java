package com.darren.ca.server.service;

public class FileUploadService implements ClientRequest {
    public int handleRequest(String request) {
        uploadFile();
        return 0;
    }

    private void uploadFile() {
        System.out.println("upload");
    }
}

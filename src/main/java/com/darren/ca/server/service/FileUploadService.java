package com.darren.ca.server.service;

import com.darren.ca.server.ClientRequest;

public class FileUploadService implements ClientRequest {
    public void handleRequest(String request) {
        uploadFile();
    }

    private void uploadFile() {
        System.out.println("upload");
    }
}

package com.darren.ca.server.service;

import com.darren.ca.server.ClientRequest;

public class FileDownloadService implements ClientRequest {
    public void handleRequest(String request) {
        downloadFile();
    }

    private void downloadFile() {
        System.out.println("download");
    }
}

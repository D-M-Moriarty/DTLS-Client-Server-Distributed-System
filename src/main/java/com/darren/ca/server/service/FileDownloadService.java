package com.darren.ca.server.service;

public class FileDownloadService implements ClientRequest {
    public void handleRequest(String request) {
        downloadFile();
    }

    private void downloadFile() {
        System.out.println("download");
    }
}

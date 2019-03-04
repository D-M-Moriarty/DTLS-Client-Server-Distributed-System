package com.darren.ca.server.service;

public class FileDownloadService implements ClientRequest {
    public int handleRequest(String request) {
        downloadFile();
        return 0;
    }

    private void downloadFile() {
        System.out.println("download");
    }
}

package com.darren.ca.server.service;

import com.darren.ca.server.payload.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.darren.ca.client.constants.ServerResponse.FILE_DOWNLOAD_FAILED;
import static com.darren.ca.client.constants.ServerResponse.FILE_DOWNLOAD_SUCCESS;

public class FileDownloadService implements ClientRequest {

    private int responseCode = FILE_DOWNLOAD_FAILED;
    private byte[] fileData;

    public Response handleRequest(String request) {
        downloadFile();
        Response response = new Response();
        response.setResponseCode(responseCode);
        response.setFileData(fileData);
        return response;
    }

    private void downloadFile() {
        File file = new File("/Users/darrenmoriarty/Desktop/testFile.txt");
        try {
            fileData = Files.readAllBytes(file.toPath());
            responseCode = FILE_DOWNLOAD_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

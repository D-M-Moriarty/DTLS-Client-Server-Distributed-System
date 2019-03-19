package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.payload.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.darren.ca.client.constants.ServerResponse.*;

public class FileDownloadService implements ClientRequest {

    private int responseCode = FILE_DOWNLOAD_FAILED;
    private byte[] fileData;
    private Response response = Response.initializeResponse();

    public Response handleRequest(DataPacket requestDataPacket) {
        if (LoggedInUsers.userIsLoggedIn(requestDataPacket)) {
            downloadFile();
            response.setResponseCode(responseCode);
            response.setFileData(fileData);
        } else {
            responseCode = PLEASE_LOGIN;
            response.setResponseCode(responseCode);
        }
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

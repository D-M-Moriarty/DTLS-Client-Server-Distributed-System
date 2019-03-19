package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;
import static com.darren.ca.server.utils.Regex.extractFilename;

public class FileDownloadService implements ClientRequest {
    private int responseCode = FILE_DOWNLOAD_FAILED;
    private Response response = Response.initializeResponse();

    public Response handleRequest(DataPacket requestDataPacket) {
        if (LoggedInUsers.userIsLoggedIn(requestDataPacket)) {
            byte[] fileData = downloadFile(requestDataPacket);
            response.setFileData(fileData);
            response.setResponseCode(responseCode);
        } else {
            responseCode = PLEASE_LOGIN;
            response.setResponseCode(responseCode);
        }
        return response;
    }

    private byte[] downloadFile(DataPacket requestDataPacket) {
        User user = LoggedInUsers.getLoggedInUser(requestDataPacket);
        String destFolder = CLIENT_DESTINATION + user.getUsername();
        String filename = extractFilename(requestDataPacket.getPayload());

        File file = new File(destFolder + "/" + filename);
        byte[] fileData = null;
        try {
            fileData = Files.readAllBytes(file.toPath());
            responseCode = FILE_DOWNLOAD_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }
}

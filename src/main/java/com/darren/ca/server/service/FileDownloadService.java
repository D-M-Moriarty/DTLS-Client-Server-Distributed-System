package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;
import static com.darren.ca.server.utils.Regex.extractFilename;

public class FileDownloadService implements ClientRequest {
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadService.class);
    private int responseCode = FILE_DOWNLOAD_FAILED;
    private Response response = Response.initializeResponse();

    @Override
    public Response handleRequest(DataPacket requestDataPacket) {
        if (LoggedInUsers.userIsLoggedIn(requestDataPacket)) {
//            if user is logged in, get byte array of file they want
            byte[] fileData = downloadFile(requestDataPacket);
//            set the fileData of the response object
            response.setFileData(fileData);
            response.setResponseCode(responseCode);
        } else {
//            user is not logged in
            responseCode = PLEASE_LOGIN;
            response.setResponseCode(responseCode);
        }
        return response;
    }

    private byte[] downloadFile(DataPacket requestDataPacket) {
//        get the current user
        User user = LoggedInUsers.getLoggedInUser(requestDataPacket);
//        get their directory on the server
        String destFolder = CLIENT_DESTINATION + user.getUsername();
//        extract the file name from the client request
        String filename = extractFilename(requestDataPacket.getPayload());
//        create a file object from the file requested
        File file = new File(destFolder + "/" + filename);
        byte[] fileData = null;
        try {
//            extract the bytes from the file
            fileData = Files.readAllBytes(file.toPath());
//            set response code to success
            responseCode = FILE_DOWNLOAD_SUCCESS;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
//        return the file data to the client
        return fileData;
    }
}

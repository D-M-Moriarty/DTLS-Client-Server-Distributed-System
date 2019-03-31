package com.darren.ca.server.service;


import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;
import static com.darren.ca.server.utils.Regex.*;

public class FileUploadService implements ClientRequest {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    private int responseCode = FILE_UPLOAD_UNSUCCESSFUL;
    private Response response = Response.initializeResponse();

    @Override
    public Response handleRequest(DataPacket requestDataPacket) {
//        extract file name, length and data from request
        String filename = extractFilename(requestDataPacket.getPayload());
        String fileLength = extractfileLength(requestDataPacket.getPayload());
        String fileData = extractfileData(requestDataPacket.getPayload());

        if (LoggedInUsers.userIsLoggedIn(requestDataPacket)) {
//            if the user is logged in upload the file
            uploadFile(fileData, fileLength, filename, requestDataPacket);
            response.setResponseCode(responseCode);
        } else {
//            respond to the client that the user isn't logged in
            responseCode = PLEASE_LOGIN;
            response.setResponseCode(responseCode);
        }
        return response;

    }

    private void uploadFile(@NotNull String fileData, String fileLength, String filename, DataPacket requestDataPacket) {
//        get the current user
        User user = LoggedInUsers.getLoggedInUser(requestDataPacket);
//        make a path with a folder of their username
        String destFolder = CLIENT_DESTINATION + user.getUsername();
//        add the file they're uploading to the end of the path
        String outputFile = destFolder + "/" + filename;
//        make the directory
        new File(destFolder).mkdirs();

        try {
//            write the binary file data to the user's directory
            Files.write(Paths.get(outputFile), fileData.getBytes());
//            respond to the client with success
            responseCode = FILE_UPLOAD_SUCCESSFUL;
        } catch (IOException e) {
//            error, the response is failed
            logger.error(e.getMessage());
        }

    }

}

package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;
import static com.darren.ca.server.utils.Regex.*;

public class FileUploadService implements ClientRequest {

    private String userFolder;
    private int responseCode = FILE_UPLOAD_FAILED;
    private Response response = Response.initializeResponse();

    @Override
    public Response handleRequest(DataPacket requestDataPacket) {
        String filename = extractFilename(requestDataPacket.getPayload());
        String fileLength = extractfileLength(requestDataPacket.getPayload());
        String fileData = extractfileData(requestDataPacket.getPayload());

        if (LoggedInUsers.userIsLoggedIn(requestDataPacket)) {
            uploadFile(fileData, fileLength, filename, requestDataPacket);
            response.setResponseCode(responseCode);
        } else {
            responseCode = PLEASE_LOGIN;
            response.setResponseCode(responseCode);
        }
        return response;

    }

    private void uploadFile(@NotNull String fileData, String fileLength, String filename, DataPacket requestDataPacket) {
        User user = LoggedInUsers.getLoggedInUser(requestDataPacket);
        String destFolder = CLIENT_DESTINATION + user.getUsername();
        String outputFile = destFolder + "/" + filename;
        new File(destFolder).mkdirs();

        try {
            Files.write(Paths.get(outputFile), fileData.getBytes());
            responseCode = FILE_UPLOAD_SUCCESSFUL;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

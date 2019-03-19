package com.darren.ca.server.service;

import com.darren.ca.server.payload.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.darren.ca.client.constants.ServerResponse.FILE_UPLOAD_FAILED;
import static com.darren.ca.client.constants.ServerResponse.FILE_UPLOAD_SUCCESSFUL;
import static com.darren.ca.server.utils.Regex.*;

public class FileUploadService implements ClientRequest {
    private static final String OUTPUT_DESTINATION = "/Users/darrenmoriarty/Desktop/";
    private static final String DESTINATION_FOLDER = "TEST_FOLDER_FTP";
    private String userFolder;
    private int responseCode = FILE_UPLOAD_FAILED;

    @Override
    public Response handleRequest(String request) {
        String filename = extractFilename(request);
        String fileLength = extractfileLength(request);
        String fileData = extractfileData(request);
        uploadFile(fileData, fileLength, filename);
        Response response = new Response();
        response.setResponseCode(responseCode);
        return response;
    }

    private void uploadFile(String fileData, String fileLength, String filename) {
        String outputFile = OUTPUT_DESTINATION + DESTINATION_FOLDER + "/" + filename;
        new File(OUTPUT_DESTINATION + DESTINATION_FOLDER).mkdirs();
        try {
            Files.write(Paths.get(outputFile), fileData.getBytes());
            responseCode = FILE_UPLOAD_SUCCESSFUL;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

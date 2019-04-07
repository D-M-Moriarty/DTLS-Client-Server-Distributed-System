package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.view.FTP_Client_GUI;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.darren.ca.client.constants.ClientStrings.DESKTOP_DEST;
import static com.darren.ca.client.constants.ClientStrings.FOLDER_EXT;
import static com.darren.ca.client.constants.ServerResponse.USER_LOGGED_OUT;
import static com.darren.ca.server.constants.RequestProtocol.*;
import static com.darren.ca.server.constants.ServerProperties.OPERATION_INDEX;

public class LoggedInState extends AbstractState implements Client {
    private static final Logger logger = LoggerFactory.getLogger(LoggedInState.class);

    public LoggedInState(FileTransferClient fileTransferClient) {
        this.fileTransferClient = fileTransferClient;
    }

    @Override
    public void login(String username, String password) {
        notifyUser(fileTransferClient.getUsername() + " is already logged in");
    }

    @Override
    public void logout(String username, String password) {
        // log the current user out of the server
        short response = processAuthRequest(username, password, LOGOUT);
        // change the clients state to logged out
        if (response == USER_LOGGED_OUT) {
            fileTransferClient.setClientState(fileTransferClient.getLoggedOutState());
            CLIENTSERVICE.closeDatagramSocket();
        }
    }

    @Override
    public void uploadFile() {
        // get the file the user wants to upload
        File file = fileTransferClient.getUploadFile();
        // create a string with the protocol filename and length
        String protocolFilenameLength = makeUploadFileName(file);
//        logger.debug(protocolFilenameLength);
        // convert the string to a byte array
        byte[] pflBytes = protocolFilenameLength.getBytes();
        try {
//            send the file name and data as a byte array and get a response from the server
            short response = getUploadResponse(file, pflBytes);
//            output the result of the upload to the user
            outputResponse(fileTransferClient.getGuiForm(), response);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void downloadFile() {
//        get the filechooser from the gui
        final JFileChooser fileChooser = fileTransferClient.getFileChooser();
//        get the file to be downloaded
        File file = fileChooser.getSelectedFile();
//        send download request to server and get response
        String downloadResponse = getDownloadResponse(fileChooser);
        logger.debug(downloadResponse);
//        extract the response code from the response
        short responseCode = Short.parseShort(downloadResponse.substring(0, OPERATION_INDEX));
//        extract the bytes of the file to a string
        String byteFileString = downloadResponse.substring(OPERATION_INDEX);
//        convert the string of bytes to a byte[]
        byte[] bytes = byteFileString.getBytes();
        logger.debug(Arrays.toString(bytes));
//        write the file to the users folder
        writeToUserFolder(bytes, file);
//        inform the user the download is complete
        outPutToGui(fileTransferClient.getGuiForm(), responseCode);
    }

    private short getUploadResponse(File file, byte[] pflBytes) throws IOException {
        byte[] fileBytes = getBytes(file);
        byte[] combinesBytes = getBytes(pflBytes, fileBytes);
        return getUploadResponse(combinesBytes);
    }

    private short getUploadResponse(byte[] combinesBytes) {
        String uploadResponse = CLIENTSERVICE.sendFileData(combinesBytes);
        logger.debug(uploadResponse);
        return Short.parseShort(uploadResponse.substring(0, OPERATION_INDEX));
    }

    private String getDownloadResponse(@NotNull JFileChooser fc) {
        File file = fc.getSelectedFile();
        String download = DOWNLOAD + "{" + file.getName() + "}";
        return CLIENTSERVICE.sendClientRequest(download);
    }

    private void writeToUserFolder(byte[] fileBytes, @NotNull File file) {
        String userFolder = DESKTOP_DEST + fileTransferClient.getUsername() + FOLDER_EXT;
        String filename = file.getName();
        String fileNameAndPath = userFolder + filename;
        new File(userFolder).mkdirs();
        try {
            Files.write(Paths.get(fileNameAndPath), fileBytes);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void outputResponse(@NotNull FTP_Client_GUI gui, short response) {
        logger.debug("The response {}", response);
        String serverResponse = getServerResponse(response);
        gui.setServerOutputTxtArea(serverResponse);
        logger.info(serverResponse);
    }

    @NotNull
    private String makeUploadFileName(@NotNull File file) {
        return UPLOAD + "{" + file.getName() + "}" + "[" + file.length() + "]";
    }

    private byte[] getBytes(byte[] pflBytes, byte[] dataBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(pflBytes);
        baos.write(dataBytes);
        return baos.toByteArray();
    }

    private byte[] getBytes(@NotNull File file) throws IOException {
        Path path = file.toPath();
//        logger.debug(String.valueOf(path));
        byte[] dataBytes = Files.readAllBytes(path);
//        logger.debug(Arrays.toString(dataBytes));
        return dataBytes;
    }

    private void outPutToGui(@NotNull FTP_Client_GUI gui, short response) {
        String serverResponse = getServerResponse(response);
        logger.info(serverResponse);
        gui.setServerOutputTxtArea(serverResponse);
    }

}

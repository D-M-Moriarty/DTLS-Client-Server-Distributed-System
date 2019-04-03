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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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
        logger.debug(protocolFilenameLength);
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
        String echo = getDownloadResponse(fileChooser);
        logger.debug(echo);
//        extract the response code from the response
        short response = Short.parseShort(echo.substring(0, OPERATION_INDEX));
//        extract the bytes to a string
        String byteString = echo.substring(OPERATION_INDEX);
//        use regex to get the array of bytes
//        byteString = Regex.extractFileBytes(byteString);
//        convert the string of bytes to a byte[]
//        byte[] b = getBytes(byteString);
        byte[] b = byteString.getBytes();
        logger.debug(Arrays.toString(b));
//        write the file to the users folder
        writeToUserFolder(b, file);
//        inform the user the download is complete
        outPutToGui(fileTransferClient.getGuiForm(), response);
    }

    private short getUploadResponse(File file, byte[] pflBytes) throws IOException {
        byte[] dataBytes = getBytes(file);
        byte[] combinesBytes = getBytes(pflBytes, dataBytes);
        return getUploadResponse(combinesBytes);
    }

    private short getUploadResponse(byte[] combinesBytes) throws IOException {
        String echo = CLIENTSERVICE.sendFileData(combinesBytes);
        logger.debug(echo);
        return Short.parseShort(echo.substring(0, 3));
    }

    private String getDownloadResponse(@NotNull JFileChooser fc) {
        File file = fc.getSelectedFile();
        String download = DOWNLOAD + "{" + file.getName() + "}";
        return CLIENTSERVICE.sendClientRequest(download);
    }

    private void writeToUserFolder(byte[] b, File file) {
        String userFolder = DESKTOP_DEST + fileTransferClient.getUsername() + FOLDER_EXT;
        String filename = file.getName();
        String all = userFolder + filename;
        new File(userFolder).mkdirs();
        try {
            Files.write(Paths.get(all), b);
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
        logger.debug(path.toString());
        byte[] dataBytes = Files.readAllBytes(path);
        logger.debug(String.valueOf(dataBytes));
        return dataBytes;
    }

    private void outPutToGui(@NotNull FTP_Client_GUI gui, short response) {
        String serverResponse = getServerResponse(response);
        logger.info(serverResponse);
        gui.setServerOutputTxtArea(serverResponse);
    }

    private byte[] getBytes(String byteString) {
        List<Integer> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(byteString, ", ");
        while (tokenizer.hasMoreElements()) {
            String val = tokenizer.nextToken().trim();
            if (!val.isEmpty()) list.add(Integer.parseInt(val));
        }
        byte[] b = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            b[i] = list.get(i).byteValue();
        }
        return b;
    }

}

package com.darren.ca.client;

import com.darren.ca.client.service.ClientService;
import com.darren.ca.client.service.FileTransferClientService;
import com.darren.ca.client.view.FTP_Client_GUI;
import com.darren.ca.server.utils.Regex;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.RequestProtocol.*;
import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class FileTransferClient implements Client {
    private FTP_Client_GUI guiForm;
    private ClientService clientService;

    FileTransferClient() {
        guiForm = new FTP_Client_GUI(this);
    }

    @Override
    public short login(String username, String password) {
        try {
            clientService = new FileTransferClientService(SERVER_ADDRESS, SERVER_PORT);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        short response = makeAuthRequest(username, password, LOGIN);
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        return response;
    }

    private String getServerResponse(short response) {
        switch (response) {
            case SUCCESSFUL_LOGIN:
                return "you logged in";
            case UNKNOWN_USER:
                return " Username not in system";
            case VALID_USER_INVALID_PASSWORD:
                return "invalid password";
            case USER_ALREADY_LOGGED_IN:
                return "Already Logged in";
            case USER_LOGGED_OUT:
                return "you logged out";
            case USER_NOT_LOGGED_IN:
                return "no user with those credentials are logged in";
            case FILE_UPLOAD_FAILED:
                return "Failed to upload file";
            case FILE_UPLOAD_SUCCESSFUL:
                return "File upload successful";
            case FILE_DOWNLOAD_FAILED:
                return "The download failed";
            case FILE_DOWNLOAD_SUCCESS:
                return "The download is complete";
            default:
                return "";
        }
    }

    private String makeCredentialsString(short protocol, String username, String password) {
        return protocol + "<" + username + ">" + "<" + password + ">";
    }

    @Override
    public short logout(String username, String password) {
        short response = makeAuthRequest(username, password, LOGOUT);
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        return response;
    }

    private short makeAuthRequest(String username, String password, short logout) {
        String credentials = makeCredentialsString(logout, username, password);
        String echo = clientService.sendClientRequest(credentials);
        System.out.println(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        System.out.println("The response " + response);
        return response;
    }

    @Override
    public short uploadFile(File file) {
        String protocolFilenameLength = UPLOAD + "{" + file.getName() + "}" + "[" + file.length() + "]";
        System.out.println(protocolFilenameLength);
        byte[] pflBytes = protocolFilenameLength.getBytes();
        try {
            Path path = file.toPath();
            System.out.println(path);
            byte[] dataBytes = Files.readAllBytes(path);
            System.out.println(dataBytes);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(pflBytes);
            baos.write(dataBytes);

            byte[] combinesBytes = baos.toByteArray();
            String echo = clientService.sendFileData(combinesBytes);
            System.out.println(echo);
            short response = Short.parseShort(echo.substring(0, 3));
            System.out.println("The response " + response);
            String serverResponse = getServerResponse(response);
            System.out.println(serverResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public short downloadFile() {
        String download = DOWNLOAD + "nothing";
        String echo = clientService.sendClientRequest(download);
        System.out.println(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        String byteString = echo.substring(3);
        byteString = Regex.extractFileBytes(byteString);
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
        System.out.println(new String(b));

        try (FileOutputStream fos = new FileOutputStream("/Users/darrenmoriarty/Desktop/newerTestFile.txt")) {
            fos.write(b);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        return 0;
    }
}

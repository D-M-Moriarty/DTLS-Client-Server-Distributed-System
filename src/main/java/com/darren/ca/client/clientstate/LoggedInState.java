package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.view.FTP_Client_GUI;
import com.darren.ca.server.utils.Regex;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.darren.ca.client.constants.ServerResponse.USER_LOGGED_OUT;
import static com.darren.ca.server.constants.RequestProtocol.*;
import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;

public class LoggedInState extends AbstractState implements Client {


    public LoggedInState(FileTransferClient fileTransferClient) {
        this.fileTransferClient = fileTransferClient;
    }

    @Override
    public void login(String username, String password) {
        FTP_Client_GUI gui = fileTransferClient.getGuiForm();
        gui.setServerOutputTxtArea(fileTransferClient.getUsername() + " is already logged in");
    }

    @Override
    public void logout(String username, String password) {
        short response = makeAuthRequest(username, password, LOGOUT);
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        FTP_Client_GUI gui = fileTransferClient.getGuiForm();
        gui.setServerOutputTxtArea(serverResponse);
        if (response == USER_LOGGED_OUT)
            fileTransferClient.setClientState(fileTransferClient.getLoggedOutState());
    }

    @Override
    public void uploadFile() {
        FTP_Client_GUI gui = fileTransferClient.getGuiForm();
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(gui.getjFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        File file = fc.getSelectedFile();
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
            String echo = fileTransferClient.getClientService().sendFileData(combinesBytes);
            System.out.println(echo);
            short response = Short.parseShort(echo.substring(0, 3));
            System.out.println("The response " + response);
            String serverResponse = getServerResponse(response);
            gui.setServerOutputTxtArea(serverResponse);
            System.out.println(serverResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadFile() {
        FTP_Client_GUI gui = fileTransferClient.getGuiForm();
        final JFileChooser fc = new JFileChooser(CLIENT_DESTINATION + "/" + fileTransferClient.getUsername());
        int returnVal = fc.showOpenDialog(gui.getjFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        File file = fc.getSelectedFile();
        String download = DOWNLOAD + "{" + file.getName() + "}";
        String echo = fileTransferClient.getClientService().sendClientRequest(download);
        System.out.println(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        String byteString = echo.substring(3);
        byteString = Regex.extractFileBytes(byteString);
        byte[] b = getBytes(byteString);
        System.out.println(new String(b));

        writeToDesktop(b);
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        gui.setServerOutputTxtArea(serverResponse);
    }

    private void writeToDesktop(byte[] b) {
        try (FileOutputStream fos = new FileOutputStream("/Users/darrenmoriarty/Desktop/newerTestFile.txt")) {
            fos.write(b);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

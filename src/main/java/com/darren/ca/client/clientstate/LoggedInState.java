package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.view.FTP_Client_GUI;
import com.darren.ca.server.utils.Regex;
import org.jetbrains.annotations.NotNull;

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
        notifyUser(fileTransferClient.getUsername() + " is already logged in");
    }

    @Override
    public void logout(String username, String password) {
        short response = processAuthRequest(username, password, LOGOUT);
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
        String protocolFilenameLength = makeUploadFileName(file);
        System.out.println(protocolFilenameLength);
        byte[] pflBytes = protocolFilenameLength.getBytes();
        try {
            short response = getResponse(file, pflBytes);
            outputResponse(gui, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short getResponse(File file, byte[] pflBytes) throws IOException {
        byte[] dataBytes = getBytes(file);
        byte[] combinesBytes = getBytes(pflBytes, dataBytes);
        return getResponse(combinesBytes);
    }

    private void outputResponse(@NotNull FTP_Client_GUI gui, short response) {
        System.out.println("The response " + response);
        String serverResponse = getServerResponse(response);
        gui.setServerOutputTxtArea(serverResponse);
        System.out.println(serverResponse);
    }

    private short getResponse(byte[] combinesBytes) throws IOException {
        String echo = CLIENTSERVICE.sendFileData(combinesBytes);
        System.out.println(echo);
        return Short.parseShort(echo.substring(0, 3));
    }

    private byte[] getBytes(byte[] pflBytes, byte[] dataBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(pflBytes);
        baos.write(dataBytes);
        return baos.toByteArray();
    }

    private byte[] getBytes(@NotNull File file) throws IOException {
        Path path = file.toPath();
        System.out.println(path);
        byte[] dataBytes = Files.readAllBytes(path);
        System.out.println(dataBytes);
        return dataBytes;
    }

    @NotNull
    private String makeUploadFileName(@NotNull File file) {
        return UPLOAD + "{" + file.getName() + "}" + "[" + file.length() + "]";
    }

    @Override
    public void downloadFile() {
        FTP_Client_GUI gui = fileTransferClient.getGuiForm();
        final JFileChooser fc = new JFileChooser(CLIENT_DESTINATION + "/" + fileTransferClient.getUsername());
        int returnVal = fc.showOpenDialog(gui.getjFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        String echo = getString(fc);
        System.out.println(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        String byteString = echo.substring(3);
        byteString = Regex.extractFileBytes(byteString);
        byte[] b = getBytes(byteString);
        System.out.println(new String(b));
        writeToDesktop(b);
        outPutToGui(gui, response);
    }

    private void outPutToGui(@NotNull FTP_Client_GUI gui, short response) {
        String serverResponse = getServerResponse(response);
        System.out.println(serverResponse);
        gui.setServerOutputTxtArea(serverResponse);
    }

    private String getString(@NotNull JFileChooser fc) {
        File file = fc.getSelectedFile();
        String download = DOWNLOAD + "{" + file.getName() + "}";
        return CLIENTSERVICE.sendClientRequest(download);
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

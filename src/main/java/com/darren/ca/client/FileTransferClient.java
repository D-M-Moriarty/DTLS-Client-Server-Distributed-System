package com.darren.ca.client;

import com.darren.ca.client.clientstate.Client;
import com.darren.ca.client.clientstate.LoggedInState;
import com.darren.ca.client.clientstate.LoggedOutState;
import com.darren.ca.client.view.FTP_Client_GUI;

import javax.swing.*;
import java.io.File;
import java.util.Deque;
import java.util.LinkedList;

import static com.darren.ca.server.constants.ServerProperties.CLIENT_DESTINATION;

public class FileTransferClient implements Client {
    private final FTP_Client_GUI guiForm;
    private String username;
    private Client clientState;
    private final Client loggedOutState;
    private final Client loggedInState;
    private final Deque<String> outputs;

    FileTransferClient() {
        guiForm = new FTP_Client_GUI(this);
        loggedInState = new LoggedInState(this);
        loggedOutState = new LoggedOutState(this);
        clientState = loggedOutState;
        outputs = new LinkedList<>();
    }

    @Override
    public void login(String username, String password) {
        setUsername(username);
        clientState.login(username, password);
    }

    @Override
    public void logout(String username, String password) {
        clientState.logout(username, password);
    }

    @Override
    public void uploadFile() {
        clientState.uploadFile();
    }

    @Override
    public void downloadFile() {
        clientState.downloadFile();
    }

    public void setClientState(Client clientState) {
        this.clientState = clientState;
    }

    public FTP_Client_GUI getGuiForm() {
        return guiForm;
    }

    public Client getLoggedOutState() {
        return loggedOutState;
    }

    public Client getLoggedInState() {
        return loggedInState;
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public void addOutput(String output) {
        if (outputs.size() > 5)
            outputs.removeLast();
        outputs.addFirst(output);
        guiForm.setServerOutputTxtArea(getOutputs());
    }

    private String getOutputs() {
        StringBuilder out = new StringBuilder();
        for (String outp : outputs)
            out.append(outp).append("\n");
        return out.toString();
    }

    public File getUploadFile() {
        FTP_Client_GUI gui = getGuiForm();
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(gui.getjFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        return fc.getSelectedFile();
    }

    public JFileChooser getFileChooser() {
        FTP_Client_GUI gui = getGuiForm();
        final JFileChooser fc = new JFileChooser(CLIENT_DESTINATION + "/" + getUsername());
        int returnVal = fc.showOpenDialog(gui.getjFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        return fc;
    }
}

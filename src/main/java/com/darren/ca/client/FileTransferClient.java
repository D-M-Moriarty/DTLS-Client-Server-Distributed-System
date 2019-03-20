package com.darren.ca.client;

import com.darren.ca.client.clientstate.Client;
import com.darren.ca.client.clientstate.LoggedInState;
import com.darren.ca.client.clientstate.LoggedOutState;
import com.darren.ca.client.service.ClientService;
import com.darren.ca.client.view.FTP_Client_GUI;

public class FileTransferClient {
    private FTP_Client_GUI guiForm;
    private ClientService clientService;
    private String username;
    private Client clientState;
    private Client loggedOutState;
    private Client loggedInState;

    FileTransferClient() {
        guiForm = new FTP_Client_GUI(this);
        loggedInState = new LoggedInState(this);
        loggedOutState = new LoggedOutState(this);
        clientState = loggedOutState;
    }

    public void login(String username, String password) {
        setUsername(username);
        clientState.login(username, password);
    }

    public void logout(String username, String password) {
        clientState.logout(username, password);
    }

    public void uploadFile() {
        clientState.uploadFile();
    }

    public void downloadFile() {
        clientState.downloadFile();
    }

    public void setClientState(Client clientState) {
        this.clientState = clientState;
    }

    public ClientService getClientService() {
        return clientService;
    }

    public void setClientService(ClientService clientService) {
        this.clientService = clientService;
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
}

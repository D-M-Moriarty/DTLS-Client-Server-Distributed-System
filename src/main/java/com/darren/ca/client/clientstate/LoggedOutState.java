package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.service.FileTransferClientService;

import java.net.SocketException;
import java.net.UnknownHostException;

import static com.darren.ca.client.constants.ClientStrings.LOGINREQUIRED;
import static com.darren.ca.client.constants.ServerResponse.SUCCESSFUL_LOGIN;
import static com.darren.ca.client.constants.ServerResponse.USER_ALREADY_LOGGED_IN;
import static com.darren.ca.server.constants.RequestProtocol.LOGIN;
import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class LoggedOutState extends AbstractState implements Client {

    public LoggedOutState(FileTransferClient fileTransferClient) {
        this.fileTransferClient = fileTransferClient;
    }

    @Override
    public void login(String username, String password) {
        try {
            CLIENTSERVICE = new FileTransferClientService(SERVER_ADDRESS, SERVER_PORT);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        short response = processAuthRequest(username, password, LOGIN);
        if (response == SUCCESSFUL_LOGIN || response == USER_ALREADY_LOGGED_IN)
            fileTransferClient.setClientState(fileTransferClient.getLoggedInState());
    }


    @Override
    public void logout(String username, String password) {
        notifyUser(LOGINREQUIRED);
    }

    @Override
    public void uploadFile() {
        notifyUser(LOGINREQUIRED);
    }

    @Override
    public void downloadFile() {
        notifyUser(LOGINREQUIRED);
    }


}

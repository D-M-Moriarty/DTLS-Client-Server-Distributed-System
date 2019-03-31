package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.service.FileTransferClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.UnknownHostException;

import static com.darren.ca.client.constants.ClientStrings.LOGINREQUIRED;
import static com.darren.ca.client.constants.ServerResponse.SUCCESSFUL_LOGIN;
import static com.darren.ca.client.constants.ServerResponse.USER_ALREADY_LOGGED_IN;
import static com.darren.ca.server.constants.RequestProtocol.LOGIN;
import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class LoggedOutState extends AbstractState implements Client {
    private static final Logger logger = LoggerFactory.getLogger(LoggedOutState.class);

    public LoggedOutState(FileTransferClient fileTransferClient) {
        this.fileTransferClient = fileTransferClient;
    }

    @Override
    public void login(String username, String password) {
        try {
            // instantiate the FileTransferClientService with the server IP and port
            CLIENTSERVICE = new FileTransferClientService(SERVER_ADDRESS, SERVER_PORT);
        } catch (SocketException | UnknownHostException e) {
            logger.error(e.getMessage());
        }
//        authorise the user with a login
        short response = processAuthRequest(username, password, LOGIN);
//        continue if the user successfully logged in, change the state of the
//        file transfer client to logged in state
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

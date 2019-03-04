package com.darren.ca.client;

import com.darren.ca.client.service.ClientService;
import com.darren.ca.client.service.FileTransferClientService;
import com.darren.ca.client.view.MyGuiForm;
import com.darren.ca.server.constants.RequestProtocol;

import java.net.SocketException;
import java.net.UnknownHostException;

import static com.darren.ca.client.constants.ServerResponse.*;
import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class FileTransferClient implements Client {
    private MyGuiForm guiForm;
    private ClientService clientService;

    FileTransferClient() throws SocketException, UnknownHostException {
        guiForm = new MyGuiForm(this);
        clientService = new FileTransferClientService(SERVER_ADDRESS, SERVER_PORT);
    }

    @Override
    public short login(String username, String password) {
        String credentials = makeCredentialsString(username, password);
        String echo = clientService.sendClientRequest(credentials);
        short response = Short.parseShort(echo.substring(0, 3));

        System.out.println("The response " + response);
        switch (response) {
            case SUCCESSFUL_LOGIN:
                System.out.println("you logged in");
                break;
            case UNKNOWN_USER:
                System.out.println(" Username not in system");
                break;
            case VALID_USER_INVALID_PASSWORD:
                System.out.println("invalid password");
                break;
            case USER_ALREADY_LOGGED_IN:
                System.out.println("Already Logged in");
            default:
                break;
        }
        return response;
    }

    private String makeCredentialsString(String username, String password) {
        return RequestProtocol.LOGIN + "<" + username + ">" + "<" + password + ">";
    }

    @Override
    public short logout(String username, String password) {
        return 0;
    }

    @Override
    public short uploadFile() {
        return 0;
    }

    @Override
    public short downloadFile() {
        return 0;
    }
}

package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.service.ClientService;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.darren.ca.client.constants.ServerResponse.*;

abstract class AbstractState implements Client {
    private static final Logger logger = LoggerFactory.getLogger(AbstractState.class);
    FileTransferClient fileTransferClient;
    static ClientService CLIENTSERVICE;

    @NotNull
    @Contract(pure = true)
    String getServerResponse(short response) {
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
                return "Failed to upload file - check file size!";
            case FILE_UPLOAD_SUCCESSFUL:
                return "File upload successful";
            case FILE_DOWNLOAD_FAILED:
                return "The download failed";
            case FILE_DOWNLOAD_SUCCESS:
                return "The download is complete";
            case PLEASE_LOGIN:
                return "Please login to perform this operation";
            default:
                return "";
        }
    }

    @NotNull
    @Contract(pure = true)
//    Creates a string to authenticate the user, used for logging in and out
    private String makeCredentialsString(short protocol, String username, String password) {
        return protocol + "<" + username + ">" + "<" + password + ">";
    }

    //    send the authorisation request to the server and interprets the response
    private short makeAuthRequest(String username, String password, short authProcess) {
        String credentials = makeCredentialsString(authProcess, username, password);
        String echo = CLIENTSERVICE.sendClientRequest(credentials);
        logger.debug(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        logger.info("The response {}", response);
        return response;
    }

    //    notify the user by updating the text area of the GUI
    void notifyUser(String s) {
        fileTransferClient.addOutput(s);
    }

    //    process the auth request for the user
    short processAuthRequest(String username, String password, short authProcess) {
        short response = makeAuthRequest(username, password, authProcess);
        String serverResponse = getServerResponse(response);
        logger.debug(serverResponse);
        notifyUser(serverResponse);
        return response;
    }
}

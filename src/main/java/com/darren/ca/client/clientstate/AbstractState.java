package com.darren.ca.client.clientstate;

import com.darren.ca.client.FileTransferClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.darren.ca.client.constants.ServerResponse.*;

abstract class AbstractState implements Client {
    FileTransferClient fileTransferClient;

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
                return "Failed to upload file";
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
    private String makeCredentialsString(short protocol, String username, String password) {
        return protocol + "<" + username + ">" + "<" + password + ">";
    }

    short makeAuthRequest(String username, String password, short authProcess) {
        String credentials = makeCredentialsString(authProcess, username, password);
        String echo = fileTransferClient.getClientService().sendClientRequest(credentials);
        System.out.println(echo);
        short response = Short.parseShort(echo.substring(0, 3));
        System.out.println("The response " + response);
        return response;
    }
}

package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUsers;

import static com.darren.ca.client.constants.ServerResponse.USER_LOGGED_OUT;
import static com.darren.ca.client.constants.ServerResponse.USER_NOT_LOGGED_IN;

public class LogoutService extends AuthService {
    // implemented template method
    @Override
    protected void userAction(String username, String password) {
        if (LoggedInUsers.userIsLoggedIn(username, password)) {
//            if the user is logged in, log them out
            LoggedInUsers.logoutUser(username, password);
            responseCode = USER_LOGGED_OUT;
        } else {
            // tell the client the user is nto logged in
            responseCode = USER_NOT_LOGGED_IN;
        }
    }
}

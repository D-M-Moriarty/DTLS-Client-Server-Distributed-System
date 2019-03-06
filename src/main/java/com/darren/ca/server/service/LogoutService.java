package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;

import static com.darren.ca.client.constants.ServerResponse.USER_LOGGED_OUT;
import static com.darren.ca.client.constants.ServerResponse.USER_NOT_LOGGED_IN;

public class LogoutService extends AuthService {
    @Override
    protected void userAction(String username, String password) {
        if (userIsLoggedIn(username, password)) {
            User user = findByUsername(username);
            LoggedInUsers.logoutUser(user);
            responseCode = USER_LOGGED_OUT;
        } else {
            responseCode = USER_NOT_LOGGED_IN;
        }
    }
}

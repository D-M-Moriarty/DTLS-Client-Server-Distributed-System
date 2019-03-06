package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;

import static com.darren.ca.client.constants.ServerResponse.*;

public class LoginService extends AuthService {
    @Override
    protected void userAction(String username, String password) {
        if (userIsLoggedIn(username, password)) {
            responseCode = USER_ALREADY_LOGGED_IN;
        } else {
            User user = findByUsername(username);
            if (user != null)
                responseCode = VALID_USER_INVALID_PASSWORD;

            if (isValidUsernameAndPassword(password, user))
                responseCode = SUCCESSFUL_LOGIN;
            LoggedInUsers.loginUser(user);
        }
    }

    private boolean isValidUsernameAndPassword(String password, User user) {
        return user != null && user.getPassword().equals(password);
    }

}

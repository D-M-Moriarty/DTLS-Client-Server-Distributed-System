package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUser;
import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import org.jetbrains.annotations.Contract;

import static com.darren.ca.client.constants.ServerResponse.*;

public class LoginService extends AuthService {
    @Override
    protected void userAction(String username, String password) {
        if (LoggedInUsers.userIsLoggedIn(username, password)) {
            responseCode = USER_ALREADY_LOGGED_IN;
        } else {
            User user = findByUsername(username);
            if (user == null)
                return;
            responseCode = VALID_USER_INVALID_PASSWORD;

            if (isValidUsernameAndPassword(password, user)) {
                responseCode = SUCCESSFUL_LOGIN;
                processLogin(user);
            }
        }
    }

    private void processLogin(User user) {
        LoggedInUser loggedInUser = new LoggedInUser(user, dataPacket);
        LoggedInUsers.loginUser(loggedInUser);
    }

    @Contract("_, null -> false")
    private boolean isValidUsernameAndPassword(String password, User user) {
        return user != null && user.getPassword().equals(password);
    }

}

package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;

import java.util.LinkedList;
import java.util.List;

import static com.darren.ca.client.constants.ServerResponse.UNKNOWN_USER;
import static com.darren.ca.server.utils.Regex.extractPassword;
import static com.darren.ca.server.utils.Regex.extractUsername;

abstract class AuthService implements ClientRequest {
    int responseCode = UNKNOWN_USER;
    private List<User> users = setUsers();
    private List<User> loggedInUsers = LoggedInUsers.getUsersLoggedIn();

    public Response handleRequest(String request) {
        String username = extractUsername(request);
        String password = extractPassword(request);

        userAction(username, password);
        Response response = new Response();
        response.setResponseCode(responseCode);
        return response;
    }

    protected abstract void userAction(String username, String password);

    boolean userIsLoggedIn(String username, String password) {
        if (!loggedInUsers.isEmpty())
            for (User user : loggedInUsers)
                if (isValidUser(username, password, user))
                    return true;
        return false;
    }

    private boolean isValidUser(String username, String password, User user) {
        return user.getUsername().equals(username) &&
                user.getPassword().equals(password);
    }

    private List<User> setUsers() {
        List<User> userList = new LinkedList<>();
        userList.add(new User("darren", "123456"));
        userList.add(new User("adam", "654321"));
        userList.add(new User("bill", "124356"));
        userList.add(new User("cat", "213465"));
        return userList;
    }

    User findByUsername(String username) {
        return users.stream().filter(uname -> uname.
                getUsername().equals(username)).findFirst().orElse(null);
    }
}

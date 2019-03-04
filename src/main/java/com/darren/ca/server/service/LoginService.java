package com.darren.ca.server.service;

import com.darren.ca.server.model.LoggedInUsers;
import com.darren.ca.server.model.User;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.darren.ca.client.constants.ServerResponse.*;

public class LoginService implements ClientRequest {

    private List<User> users = setUsers();
    private int responseCode = UNKNOWN_USER;
    private List<User> loggedInUsers = LoggedInUsers.getUsersLoggedIn();

    public int handleRequest(String request) {
        String username = extractUsername(request);
        String password = extractPassword(request);

        if (userIsAlreadyLoggedIn(username, password))
            return USER_ALREADY_LOGGED_IN;

        login(username, password);
        return responseCode;
    }

    private boolean userIsAlreadyLoggedIn(String username, String password) {
        if (loggedInUsers == null)
            return false;
        for (User user : loggedInUsers) {
            if (user.getUsername().equals(username) &&
                    user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    private void login(String username, String password) {
        User user = findByUsername(username);
        if (user != null)
            responseCode = VALID_USER_INVALID_PASSWORD;

        if (isValidUsernameAndPassword(password, user))
            responseCode = SUCCESSFUL_LOGIN;

        LoggedInUsers.loginUser(user);
    }

    private boolean isValidUsernameAndPassword(String password, User user) {
        return user != null && user.getPassword().equals(password);
    }

    private String extractPassword(String request) {
        String regex = "<(?<username>.*?)><(?<password>.*?)>";
        String group = "password";
        return getRegexMatch(request, regex, group);
    }

    private String extractUsername(String request) {
        String regex = "<(?<username>.*?)><";
        String group = "username";
        return getRegexMatch(request, regex, group);
    }

    private String getRegexMatch(String request, String regex, String group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(request);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return "no user";
    }

    private List<User> setUsers() {
        List<User> userList = new LinkedList<>();
        userList.add(new User("darren", "123456"));
        userList.add(new User("adam", "654321"));
        userList.add(new User("bill", "124356"));
        userList.add(new User("cat", "213465"));
        return userList;
    }

    private User findByUsername(String username) {
        return users.stream().filter(uname -> uname.
                getUsername().equals(username)).findFirst().orElse(null);
    }
}

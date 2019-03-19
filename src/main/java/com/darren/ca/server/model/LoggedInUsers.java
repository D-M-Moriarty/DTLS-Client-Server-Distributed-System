package com.darren.ca.server.model;

import java.util.ArrayList;
import java.util.List;

public enum LoggedInUsers {
    INSTANCE;
    private static List<LoggedInUser> usersLoggedIn = new ArrayList<>();

    public static List<LoggedInUser> getUsersLoggedIn() {
        return usersLoggedIn;
    }

    public static void loginUser(LoggedInUser user) {
        usersLoggedIn.add(user);
    }

    private static List<User> getAllLoggedIn() {
        List<User> users = new ArrayList<>();
        for (LoggedInUser user : usersLoggedIn)
            users.add(user.getUser());
        return users;
    }

    public static boolean userIsLoggedIn(String username, String password) {
        for (User user : getAllLoggedIn())
            if (user.getPassword().equals(password) && user.getUsername().equals(username))
                return true;
        return false;
    }

    private static boolean isLoggedInClient(DataPacket dataPacket, LoggedInUser loggedInUser) {
        return loggedInUser.getClientIP().equals(dataPacket.getHost()) &&
                loggedInUser.getClientPortNum() == dataPacket.getPort();
    }

    public static void logoutUser(String username, String password) {
        LoggedInUser userToRemove = LoggedInUser.getNullUser();
        for (LoggedInUser loggedInUser : usersLoggedIn)
            if (checkForUser(username, password, loggedInUser)) {
                userToRemove = loggedInUser;
                break;
            }
        usersLoggedIn.remove(userToRemove);
    }

    private static boolean checkForUser(String username, String password, LoggedInUser loggedInUser) {
        return loggedInUser.getUser().getUsername().equals(username) &&
                loggedInUser.getUser().getPassword().equals(password);
    }

    public static boolean userIsLoggedIn(DataPacket dataPacket) {
        for (LoggedInUser loggedInUser : usersLoggedIn)
            if (isLoggedInClient(dataPacket, loggedInUser))
                return true;
        return false;
    }
}

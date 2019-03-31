package com.darren.ca.server.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public enum LoggedInUsers {
    INSTANCE;
    private static Set<LoggedInUser> usersLoggedIn = new HashSet<>();
    public static void loginUser(LoggedInUser user) {
        usersLoggedIn.add(user);
    }

    private static Set<User> getAllLoggedIn() {
        Set<User> users = new HashSet<>();
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

    private static boolean isLoggedInClient(@NotNull DataPacket dataPacket, @NotNull LoggedInUser loggedInUser) {
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

    private static boolean checkForUser(String username, String password, @NotNull LoggedInUser loggedInUser) {
        return loggedInUser.getUser().getUsername().equals(username) &&
                loggedInUser.getUser().getPassword().equals(password);
    }

    public static boolean userIsLoggedIn(DataPacket dataPacket) {
        for (LoggedInUser loggedInUser : usersLoggedIn)
            if (isLoggedInClient(dataPacket, loggedInUser))
                return true;
        return false;
    }

    @Nullable
    public static User getLoggedInUser(DataPacket dataPacket) {
        for (LoggedInUser loggedInUser : usersLoggedIn)
            if (isLoggedInClient(dataPacket, loggedInUser))
                return loggedInUser.getUser();
        return null;
    }
}

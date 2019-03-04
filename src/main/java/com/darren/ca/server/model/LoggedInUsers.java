package com.darren.ca.server.model;

import java.util.ArrayList;
import java.util.List;

public enum LoggedInUsers {
    INSTANCE;
    private static List<User> usersLoggedIn = new ArrayList<>();

    public static List<User> getUsersLoggedIn() {
        return usersLoggedIn;
    }

    public static void loginUser(User user) {
        usersLoggedIn.add(user);
    }

    public static void logoutUser(User user) {
        usersLoggedIn.remove(user);
    }

    public static void addUserGroup(List<User> users) {
        usersLoggedIn.addAll(users);
    }
}

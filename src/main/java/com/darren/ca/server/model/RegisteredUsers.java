package com.darren.ca.server.model;

import java.util.HashMap;
import java.util.Map;

public class RegisteredUsers {
    private static final RegisteredUsers registeredUsers = new RegisteredUsers();
    private static Map<String, User> registered;

    private RegisteredUsers() {
        registered = new HashMap<>();
        registered.put("darren", new User("darren", "123456"));
        registered.put("adam", new User("adam", "654321"));
        registered.put("bill", new User("bill", "124356"));
        registered.put("cat", new User("cat", "213465"));
    }

    public static Map<String, User> getRegisteredUser() {
        return registered;
    }

    public User checkRegisteredUser(String username) {
        return registered.get(username);
    }
}
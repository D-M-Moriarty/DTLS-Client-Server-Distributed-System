package com.darren.ca.server.service;

public class LogoutService implements ClientRequest {
    public int handleRequest(String request) {
        logout();
        return 0;
    }

    private void logout() {
        System.out.println("logout");
    }
}

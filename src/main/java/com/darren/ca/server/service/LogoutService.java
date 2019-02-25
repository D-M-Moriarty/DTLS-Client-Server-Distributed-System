package com.darren.ca.server.service;

public class LogoutService implements ClientRequest {
    public void handleRequest(String request) {
        logout();
    }

    private void logout() {
        System.out.println("logout");
    }
}

package com.darren.ca.server.service;

import com.darren.ca.server.ClientRequest;

public class LogoutService implements ClientRequest {
    public void handleRequest(String request) {
        logout();
    }

    private void logout() {
        System.out.println("logout");
    }
}

package com.darren.ca.server.service;

import com.darren.ca.server.ClientRequest;

public class LoginService implements ClientRequest {
    public void handleRequest(String request) {
        login();
    }

    private void login() {
        System.out.println("login");
    }
}

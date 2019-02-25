package com.darren.ca.server.service;

public class LoginService implements ClientRequest {
    public void handleRequest(String request) {
        login();
    }

    private void login() {
        System.out.println("login");
    }
}

package com.darren.ca.server;

public class StartServer {
    private StartServer() {
    }

    public static void main(String[] args) {
        new Server().start(args);
    }
}

package com.darren.ca.server;

import com.darren.ca.server.factory.StandardClientRequest;

public class StartServer {
    private StartServer() {
    }

    public static void main(String[] args) {
        Server server = Server.getServer();
        server.start(args, new StandardClientRequest());
    }
}

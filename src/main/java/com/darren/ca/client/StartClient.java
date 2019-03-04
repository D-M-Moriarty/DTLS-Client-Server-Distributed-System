package com.darren.ca.client;

import java.net.SocketException;
import java.net.UnknownHostException;

public class StartClient {

    private StartClient() {
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        new FileTransferClient();
    }
}

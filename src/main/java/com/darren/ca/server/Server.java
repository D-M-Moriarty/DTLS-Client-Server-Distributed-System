package com.darren.ca.server;

import com.darren.ca.DatagramMessage;

import java.io.IOException;

public class Server {

    public void start(String[] port) {
        int serverPort = 7;    // default port
        if (port.length == 1)
            serverPort = Integer.parseInt(port[0]);
        connect(serverPort);
    }

    public void start() {
        int serverPort = 7;
        connect(serverPort);
    }

    private void connect(int serverPort) {
        try {
            // instantiates a datagram socket for both sending
            // and receiving data
            MyServerDatagramSocket mySocket = new MyServerDatagramSocket(serverPort);
            System.out.println("Echo server ready.");

            waitForRequests(mySocket);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        } // end catch
    }

    private void waitForRequests(MyServerDatagramSocket mySocket) throws IOException {
        do {
            DatagramMessage request = mySocket.receiveMessageAndSender();
            System.out.println("Request received");
            String message = request.getMessage().trim();

            handleRequest(message);

            System.out.println("message received: " + message);
            // Now send the echo to the requestor
            mySocket.sendMessage(request.getAddress(),
                    request.getPort(), message);
        } while (true);
    }

    private void handleRequest(String message) {
        if ("login".equals(message)) {
            System.out.println("Attempt Login");
        } else if ("logout".equals(message)) {
            System.out.println("Logout of system");
        }
    }
}

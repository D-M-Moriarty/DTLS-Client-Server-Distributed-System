package com.darren.ca.server;

import com.darren.ca.DatagramMessage;
import com.darren.ca.server.factory.RequestFactory;

import java.io.IOException;

public class Server {
    private static Server server = new Server();
    private RequestFactory requestFactory;
    private ClientRequest clientRequest;

    private Server() {
    }

    static Server getServer() {
        return server;
    }

    void start(String[] port, RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        if (port.length == 1) {
            int serverPort = Integer.parseInt(port[0]);
            connect(serverPort);
        }
    }

    public void start() {
        int serverPort = 7;
        connect(serverPort);
    }

    private void connect(int serverPort) {
        try {
            MyServerDatagramSocket mySocket = new MyServerDatagramSocket(serverPort);
            System.out.println("Echo server ready.");
            waitForRequests(mySocket);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void waitForRequests(MyServerDatagramSocket mySocket) throws IOException {
        do {
            DatagramMessage request = mySocket.receiveMessageAndSender();
            System.out.println("Request received");
            String message = request.getMessage().trim();

            clientRequest = requestFactory.getClientRequest(message);
            clientRequest.handleRequest(message);

            System.out.println("message received: " + message);
            // Now send the echo to the requestor
            mySocket.sendMessage(request.getAddress(),
                    request.getPort(), message);
        } while (true);
    }

}

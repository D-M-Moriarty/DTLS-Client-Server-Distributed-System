package com.darren.ca.server;

import com.darren.ca.DatagramMessage;
import com.darren.ca.server.factory.RequestFactory;
import com.darren.ca.server.payload.ServerDatagramSocket;
import com.darren.ca.server.payload.ServerSocketDatagram;
import com.darren.ca.server.service.ClientRequest;

import java.io.IOException;

public class Server {
    private static Server server = new Server();
    private RequestFactory requestFactory;
    private static final Integer DEFAULT_PORT = 3000;

    private Server() {
    }

    static Server getServer() {
        return server;
    }

    void start(String[] port, RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        int serverPort = setPort(port);
        connect(serverPort);
    }

    public void start(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        connect(DEFAULT_PORT);
    }

    private int setPort(String[] port) {
        return port.length == 1 ? Integer.parseInt(port[0]) : DEFAULT_PORT;
    }

    private void connect(int serverPort) {
        try {
            ServerSocketDatagram mySocket = new ServerDatagramSocket(serverPort);
            System.out.println("Echo server ready.");
            waitForRequests(mySocket);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void waitForRequests(ServerSocketDatagram mySocket) throws IOException {
        do {
            DatagramMessage request = mySocket.receiveMessageAndSender();
            System.out.println("Request received");
            String message = request.getMessage().trim();

            ClientRequest clientRequest = requestFactory.getClientRequest(message);
            clientRequest.handleRequest(message);

            System.out.println("message received: " + message.trim());
            // Now send the echo to the requestor
            mySocket.sendFile(request.getAddress(), request.getPort(), message.getBytes());
        } while (true);
    }

}

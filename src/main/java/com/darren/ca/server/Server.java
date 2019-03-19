package com.darren.ca.server;

import com.darren.ca.server.factory.RequestFactory;
import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.payload.Response;
import com.darren.ca.server.payload.ServerDatagramSocket;
import com.darren.ca.server.payload.ServerSocketDatagram;
import com.darren.ca.server.service.ClientRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class Server {
    private static Server server = new Server();
    private RequestFactory requestFactory;

    private Server() {
    }

    @Contract(pure = true)
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
        connect(SERVER_PORT);
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

    private void waitForRequests(@NotNull ServerSocketDatagram mySocket) throws IOException {
        for (; ; ) {
            DataPacket requestData = mySocket.receiveMessageAndSender();
            System.out.println("Request received");
            short operationCode = requestData.getOperationCode();
            String payload = requestData.getPayload();

            ClientRequest clientRequest = requestFactory.getClientRequest(operationCode);
            Response response = clientRequest.handleRequest(requestData);

            System.out.println("message received: " + requestData.getMessage());
            mySocket.sendFile(requestData.getHost(), requestData.getPort(), response.getResponseBytes());
        }
    }

    private int setPort(@NotNull String[] port) {
        return port.length == 1 ? Integer.parseInt(port[0]) : SERVER_PORT;
    }

}

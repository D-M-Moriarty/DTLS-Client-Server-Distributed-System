package com.darren.ca.client.service;

import com.darren.ca.client.request.ClientDatagramSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FileTransferClientService implements ClientService {
    private ClientDatagramSocket datagramSocket;
    private InetAddress serverHost;
    private int serverPort;

    public FileTransferClientService(String serverHost, int serverPort) throws SocketException, UnknownHostException {
        this.datagramSocket = new ClientDatagramSocket();
        this.serverHost = InetAddress.getByName(serverHost);
        ;
        this.serverPort = serverPort;
    }

    @Override
    public String sendClientRequest(String request) {
        try {
            datagramSocket.sendDatagramPacket(serverHost, serverPort, request);
            return datagramSocket.receiveServerResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void closeDatagramSocket() {
        datagramSocket.closeSocket();
    }
}

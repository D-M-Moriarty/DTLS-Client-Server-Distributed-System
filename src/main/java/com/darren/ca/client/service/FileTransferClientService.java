package com.darren.ca.client.service;

import com.darren.ca.client.FileTransferClient;
import com.darren.ca.client.request.ClientDTLSSocket;
import com.darren.ca.client.request.ClientSocketDatagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static com.darren.ca.client.constants.ServerResponse.FILE_UPLOAD_FAILED;

public class FileTransferClientService implements ClientService {
    private static final Logger logger = LoggerFactory.getLogger(FileTransferClient.class);
    private ClientSocketDatagram datagramSocket;
    private InetAddress serverHost;
    private int serverPort;

    public FileTransferClientService(String serverHost, int serverPort) throws SocketException, UnknownHostException {
        this.datagramSocket = new ClientDTLSSocket();
        this.serverHost = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
    }

    @Override
    public String sendClientRequest(String request) {
        try {
//            send a request as a string
            datagramSocket.sendDatagramMessage(serverHost, serverPort, request);
            return datagramSocket.receiveServerResponse();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    @Override
    public String sendFileData(byte[] fileBytes) {
        try {
//            send binary data
            datagramSocket.sendDatagramPacket(serverHost, serverPort, fileBytes);
            return datagramSocket.receiveServerResponse();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return String.valueOf(FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void closeDatagramSocket() {
        datagramSocket.closeSocket();
    }
}

package com.darren.ca.client.request;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientDatagramSocket implements ClientSocketDatagram {
    private static final int MAX_LENGTH = 100;
    private DatagramSocket datagramSocket;
    private SSLSocket sslSocket;

    public ClientDatagramSocket() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    public ClientDatagramSocket(String serverHost, int serverPort) {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverHost, serverPort);
            sslSocket.startHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message)
            throws IOException {
        byte[] sendBuffer = message.getBytes();
        sendDatagramPacket(receiverHost, receiverPort, sendBuffer);
    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data)
            throws IOException {
        DatagramPacket datagram = new DatagramPacket(
                data, data.length, receiverHost, receiverPort
        );
        datagramSocket.send(datagram);
    }

    @Override
    public String receiveServerResponse() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LENGTH];
        DatagramPacket datagram = new DatagramPacket(
                receiveBuffer, MAX_LENGTH
        );
        datagramSocket.receive(datagram);
        return new String(receiveBuffer);
    }

    @Override
    public void closeSocket() {
        datagramSocket.close();
    }
}

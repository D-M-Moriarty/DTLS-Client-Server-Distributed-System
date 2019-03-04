package com.darren.ca.client.request;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientDatagramSocket implements ClientSocketDatagram {
    private static final int MAX_LENGTH = 100;
    private DatagramSocket datagramSocket;

    public ClientDatagramSocket() throws SocketException {
        datagramSocket = new DatagramSocket();
    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, String message)
            throws IOException {
        byte[] sendBuffer = message.getBytes();
        DatagramPacket datagram = new DatagramPacket(
                sendBuffer, sendBuffer.length, receiverHost, receiverPort
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

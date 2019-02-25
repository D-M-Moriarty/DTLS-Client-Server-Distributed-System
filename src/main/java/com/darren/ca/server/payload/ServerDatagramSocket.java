package com.darren.ca.server.payload;

import com.darren.ca.DatagramMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerDatagramSocket implements ServerSocketDatagram {
    private static final int MAX_LEN = 100;
    private DatagramSocket socket;

    public ServerDatagramSocket(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
    }

    public DatagramMessage receiveMessageAndSender() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
        socket.receive(datagram);
        DatagramMessage returnVal = new DatagramMessage();
        returnVal.putVal(new String(receiveBuffer),
                datagram.getAddress(),
                datagram.getPort());
        return returnVal;
    }

    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException {
        DatagramPacket datagram = new DatagramPacket(file, file.length, receiverHost, receiverPort);
        socket.send(datagram);
    }

}

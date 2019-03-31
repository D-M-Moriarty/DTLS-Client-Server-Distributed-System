package com.darren.ca.server.payload;

import com.darren.ca.server.model.DataPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerDatagramSocket implements ServerSocketDatagram {
    //    https://en.wikipedia.org/wiki/User_Datagram_Protocol
    private static final int MAX_LEN = 65507;
    private DatagramSocket socket;

    public ServerDatagramSocket(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
    }

    @Override
    public DataPacket receiveMessageAndSender() throws IOException {
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);
        socket.receive(datagram);
        return new DataPacket(datagram.getAddress(), datagram.getPort(), new String(datagram.getData()));
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException {
        DatagramPacket datagram = new DatagramPacket(file, file.length, receiverHost, receiverPort);
        socket.send(datagram);
    }

}

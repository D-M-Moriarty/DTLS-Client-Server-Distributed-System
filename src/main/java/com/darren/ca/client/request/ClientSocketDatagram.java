package com.darren.ca.client.request;

import java.io.IOException;
import java.net.InetAddress;

public interface ClientSocketDatagram {
    void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException;
    void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data) throws IOException;
    String receiveServerResponse() throws IOException;
    void closeSocket();
}

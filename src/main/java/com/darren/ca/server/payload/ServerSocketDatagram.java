package com.darren.ca.server.payload;

import com.darren.ca.DatagramMessage;

import java.io.IOException;
import java.net.InetAddress;

public interface ServerSocketDatagram {
    DatagramMessage receiveMessageAndSender() throws IOException;

    void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException;
}

package com.darren.ca.server.payload;

import com.darren.ca.server.model.DataPacket;

import java.io.IOException;
import java.net.InetAddress;

public interface ServerSocketDatagram {
    DataPacket receiveMessageAndSender() throws IOException;

    void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException;
}

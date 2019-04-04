package com.darren.ca.client.request;

import com.darren.ca.dtls.DTLS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientDTLSSocket implements ClientSocketDatagram {
    private static final Logger logger = LoggerFactory.getLogger(ClientDTLSSocket.class);
    private SSLEngine sslEngine;
    private DatagramSocket socket;
    private DTLS dtls;

    public ClientDTLSSocket() throws SocketException {
        this.socket = new DatagramSocket();
    }

    @Override
    public void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message) {
        byte[] sendBuffer = message.getBytes();
        sendDatagramPacket(receiverHost, receiverPort, sendBuffer);
    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data) {
        SocketAddress serverAddress = new InetSocketAddress(receiverHost, receiverPort);
        dtls = DTLS.createDTLS();
        try {
            sslEngine = dtls.createSSLEngine(true);
            dtls.handshake(sslEngine, socket, serverAddress, "Client");
            dtls.deliverAppData(
                    sslEngine,
                    socket,
                    ByteBuffer.wrap(data),
                    serverAddress
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public String receiveServerResponse() {
        try {
            return getDTLSMessage();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "";
    }

    @NotNull
    private String getDTLSMessage() throws Exception {
        // gets the buffer data in a byte array and returns it as a string
        return new String(dtls.receiveAppData(sslEngine, socket)
                .getBufferBytes()).replaceAll("\0", "");
    }

    @Override
    public void closeSocket() {
        socket.close();
    }
}

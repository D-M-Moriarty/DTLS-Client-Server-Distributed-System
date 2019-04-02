package com.darren.ca.client.request;

import com.darren.ca.server.payload.DTLS;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientDTLSSocket implements ClientSocketDatagram {
    private SSLEngine sslEngine;
    private DatagramSocket socket;
    private DTLS dtls;

    public ClientDTLSSocket() throws SocketException {
        this.socket = new DatagramSocket();
        dtls = new DTLS();
        try {
            sslEngine = dtls.createSSLEngine(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
        byte[] sendBuffer = message.getBytes();
        sendDatagramPacket(receiverHost, receiverPort, sendBuffer);
    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data) throws IOException {
        SocketAddress serverAddress = new InetSocketAddress(receiverHost, receiverPort);
        dtls = new DTLS();
        try {
            sslEngine = dtls.createSSLEngine(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            dtls.handshake(sslEngine, socket, serverAddress, "Client");
            dtls.deliverAppData(
                    sslEngine,
                    socket,
                    ByteBuffer.wrap(data),
                    serverAddress
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String receiveServerResponse() throws IOException {
        ByteBuffer dataBuffer = null;
        try {
            dataBuffer = dtls.receiveAppData(sslEngine, socket, null).getByteBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataBuffer == null ? "" : new String(dataBuffer.array());
    }

    @Override
    public void closeSocket() {
        socket.close();
    }
}

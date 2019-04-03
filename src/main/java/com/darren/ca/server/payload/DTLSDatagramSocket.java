package com.darren.ca.server.payload;

import com.darren.ca.dtls.DTLS;
import com.darren.ca.dtls.DTLSReceive;
import com.darren.ca.server.model.DataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.net.*;
import java.nio.ByteBuffer;

public class DTLSDatagramSocket implements ServerSocketDatagram {
    private static final Logger logger = LoggerFactory.getLogger(DTLSDatagramSocket.class);
    private SSLEngine sslEngine;
    private DatagramSocket socket;
    private ByteBuffer clientApp;
    private DTLS dtls;
    private DatagramPacket datagramPacket = null;

    public DTLSDatagramSocket(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public DataPacket receiveMessageAndSender() throws Exception {
        dtls = DTLS.createDTLS();
        try {
            sslEngine = dtls.createSSLEngine(false);
            SSLSession sslSession = sslEngine.getSession();
            clientApp = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        dtls.serverHandshake(sslEngine, socket, "Server");
        DTLSReceive dtlsReceive = dtls.receiveAppData(sslEngine, socket, clientApp);
        datagramPacket = dtlsReceive.getDatagramPacket();
        return new DataPacket(
                datagramPacket.getAddress(),
                datagramPacket.getPort(),
                new String(dtlsReceive.getByteBuffer().array()).replaceAll("\0", "")
        );
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) {
        try {
            dtls.deliverAppData(
                    sslEngine,
                    socket,
                    ByteBuffer.wrap(file),
                    new InetSocketAddress(InetAddress.getLocalHost(), datagramPacket.getPort())
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}

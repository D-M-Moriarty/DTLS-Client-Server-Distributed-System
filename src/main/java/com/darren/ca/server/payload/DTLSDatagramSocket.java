package com.darren.ca.server.payload;

import com.darren.ca.dtls.DTLS;
import com.darren.ca.dtls.DTLSReceive;
import com.darren.ca.server.model.DataPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.net.*;
import java.nio.ByteBuffer;

public class DTLSDatagramSocket implements ServerSocketDatagram {
    private static final Logger logger = LoggerFactory.getLogger(DTLSDatagramSocket.class);
    private SSLEngine sslEngine;
    private DatagramSocket socket;
    private DTLS dtls;
    private DatagramPacket datagramPacket = null;

    public DTLSDatagramSocket(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public DataPacket receiveMessageAndSender() throws Exception {
        dtls = DTLS.createDTLS();
        sslEngine = dtls.createSSLEngine(false);
        dtls.serverHandshake(sslEngine, socket, "Server");
        DTLSReceive dtlsReceive = dtls.receiveAppData(sslEngine, socket);
        datagramPacket = dtlsReceive.getDatagramPacket();
        return new DataPacket(
                datagramPacket.getAddress(),
                datagramPacket.getPort(),
                new String(dtlsReceive.getBufferBytes()).replaceAll("\0", "")
        );
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) {
        try {
            dtls.deliverAppData(
                    sslEngine,
                    socket,
                    ByteBuffer.wrap(file),
                    new InetSocketAddress(
                            datagramPacket.getAddress(),
                            datagramPacket.getPort()
                    )
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}

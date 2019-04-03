package com.darren.ca.server.bouncycastle;

import org.bouncycastle.crypto.tls.DTLSServerProtocol;
import org.bouncycastle.crypto.tls.DTLSTransport;
import org.bouncycastle.crypto.tls.DatagramTransport;
import org.bouncycastle.crypto.tls.UDPTransport;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_PASSWORD;

public class bcServer {
    private static final int MAX_LEN = 100;
    private static int MAX_HANDSHAKE_LOOPS = 60;
    private DatagramSocket datagramSocket;
    private char[] keystorePassword = KEYSTORE_PASSWORD.toCharArray();
    private String keystoreName = KEYSTORE_NAME;
    private char[] ctPass = KEYSTORE_PASSWORD.toCharArray();
    private KeyStore keyStoreKeys;
    private KeyStore keyStoreTrust;
    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;
    private SSLContext sslContext;
    private SSLEngine sslEngine;
    private DatagramSocket socket;
    private SSLEngine engine;

    public bcServer() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        keyStoreKeys = KeyStore.getInstance("PKCS12");

        keyStoreKeys.load(new FileInputStream(keystoreName), keystorePassword);
        keyStoreTrust = KeyStore.getInstance("PKCS12");
        keyStoreTrust.load(new FileInputStream(keystoreName), keystorePassword);

        // KeyManagers decide which key material to use
        keyManagerFactory = KeyManagerFactory.getInstance("PKIX");
        keyManagerFactory.init(keyStoreKeys, keystorePassword);

        // TrustManagers decide whether to allow connections
        trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init(keyStoreTrust);

        // Get an SSLContext for DTLS Protocol without authentication
        sslContext = SSLContext.getInstance("DTLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        int port = 3000;

        int mtu = 1500;

        SecureRandom secureRandom = new SecureRandom();

        DTLSServerProtocol serverProtocol = new DTLSServerProtocol(secureRandom);

        byte[] data = new byte[mtu];
        DatagramPacket packet = new DatagramPacket(data, mtu);

        DatagramSocket socket = new DatagramSocket(port);
        socket.receive(packet);

        System.out.println("Accepting connection from " + packet.getAddress().getHostAddress() + ":" + port);
        socket.connect(packet.getAddress(), packet.getPort());

        /*
         * NOTE: For simplicity, and since we don't yet have HelloVerifyRequest support, we just
         * discard the initial packet, which the client should re-send anyway.
         */

        DatagramTransport transport = new UDPTransport(socket, mtu);

        // Uncomment to see packets
//        transport = new LoggingDatagramTransport(transport, System.out);

        MockDTLSServer server = new MockDTLSServer();
        DTLSTransport dtlsServer = serverProtocol.accept(server, transport);

        byte[] buf = new byte[dtlsServer.getReceiveLimit()];

        while (!socket.isClosed()) {
            try {
                int length = dtlsServer.receive(buf, 0, buf.length, 60000);
                if (length >= 0) {
                    System.out.write(buf, 0, length);
                    dtlsServer.send(buf, 0, length);
                }
            } catch (SocketTimeoutException ste) {
            }
        }

        dtlsServer.close();
    }

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        new bcServer();
    }
}

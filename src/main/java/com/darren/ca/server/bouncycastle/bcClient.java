package com.darren.ca.server.bouncycastle;

import org.bouncycastle.crypto.tls.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_PASSWORD;

public class bcClient {

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

    public bcClient() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
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
        DatagramSocket socket = new DatagramSocket();
        socket.connect(InetAddress.getLocalHost(), 3000);
        int mtu = 1500;
        DatagramTransport transport = new UDPTransport(socket, mtu);

        TlsSession session = createSession(InetAddress.getLocalHost(), 3000);

        MockDTLSClient client = new MockDTLSClient(session);
        DTLSClientProtocol protocol = new DTLSClientProtocol(new SecureRandom());
        DTLSTransport dtlsTransport = protocol.connect(client, transport);

        dtlsTransport.send("101Hello".getBytes(), "101Hello".length(), 100);

        dtlsTransport.close();
    }

    private static TlsSession createSession(InetAddress address, int port)
            throws IOException {
        MockDTLSClient client = new MockDTLSClient(null);
        DTLSTransport dtls = openDTLSConnection(address, port, client);
        TlsSession session = client.getSessionToResume();
        dtls.close();
        return session;
    }

    private static DTLSTransport openDTLSConnection(InetAddress address, int port, TlsClient client)
            throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.connect(address, port);

        int mtu = 1500;
        DatagramTransport transport = new UDPTransport(socket, mtu);

        DTLSClientProtocol protocol = new DTLSClientProtocol(new SecureRandom());

        return protocol.connect(client, transport);
    }

    public static void main(String[] args) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        new bcClient();
    }
}

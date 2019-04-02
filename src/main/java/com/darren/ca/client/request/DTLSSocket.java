package com.darren.ca.client.request;

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

import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;
import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_PASSWORD;

public class DTLSSocket implements ClientSocketDatagram {

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

    public DTLSSocket() {
        // Create and initialize the SSLContext with key mat

        // First initialize the key and trust material
        KeyStore ksKeys = null;
        try {
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

            // Create the engine
            engine = sslContext.createSSLEngine(SERVER_ADDRESS, SERVER_PORT);
            datagramSocket = new DatagramSocket();
            datagramSocket.connect(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            // Use engine as client
            engine.setUseClientMode(true);


        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {

    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data) throws IOException {
        DatagramTransport transport = new UDPTransport(datagramSocket, 1000);
        TlsClient client = new DefaultTlsClient() {
            @Override
            public TlsAuthentication getAuthentication() throws IOException {
                return null;
            }
        };
        DTLSClientProtocol protocol = new DTLSClientProtocol(new SecureRandom());
        DTLSTransport dtls = protocol.connect(client, transport);
        dtls.send(data, 100, 100);
    }

    @Override
    public String receiveServerResponse() throws IOException {
        return null;
    }

    @Override
    public void closeSocket() {

    }
}

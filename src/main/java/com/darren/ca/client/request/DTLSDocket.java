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

public class DTLSDocket implements ClientSocketDatagram {

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

    public DTLSDocket() {
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
        DatagramTransport transport = new UDPTransport(datagramSocket, 1000);
        TlsClient client = new DefaultTlsClient() {
            @Override
            public TlsAuthentication getAuthentication() throws IOException {
                return null;
            }
        };
        DTLSClientProtocol protocol = new DTLSClientProtocol(new SecureRandom());
        DTLSTransport dtls = protocol.connect(client, transport);
        dtls.send(message.getBytes(), 100, 100);
//
//        // Create a nonblocking socket channel
//        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
//        socketChannel.connect(new InetSocketAddress(receiverHost, receiverPort));
//
//        // Complete connection
//        while (!socketChannel.finishConnect()) {
//            // do something until connect completed
//            System.out.println("Connecting");
//        }
//
//        //Create byte buffers for holding application and encoded data
//
//        SSLSession session = engine.getSession();
//        ByteBuffer myAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
//        ByteBuffer myNetData = ByteBuffer.allocate(session.getPacketBufferSize());
//        ByteBuffer peerAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
//        ByteBuffer peerNetData = ByteBuffer.allocate(session.getPacketBufferSize());
//
//        // Do initial handshake
//        engine.beginHandshake();
//
//        myAppData.put("hello".getBytes());
//        myAppData.flip();
//
//        while (myAppData.hasRemaining()) {
//            // Generate SSL/TLS/DTLS encoded data (handshake or application data)
//            SSLEngineResult res = engine.wrap(myAppData, myNetData);
//
//            // Process status of call
//            if (res.getStatus() == SSLEngineResult.Status.OK) {
//                myAppData.compact();
//
//                // Send SSL/TLS/DTLS encoded data to peer
//                while(myNetData.hasRemaining()) {
//                    int num = socketChannel.write(myNetData);
//                    if (num == 0) {
//                        // no bytes written; try again later
//                    }
//                }
//            }
//
//        }
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

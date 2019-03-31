package com.darren.ca.server.payload;

import com.darren.ca.server.model.DataPacket;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_PASSWORD;

public class Tester implements ServerSocketDatagram {
    private static final int MAX_LEN = 100;
    private static int MAX_HANDSHAKE_LOOPS = 60;
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

    public Tester(int serverPort) {
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
            engine = sslContext.createSSLEngine("localhost", serverPort);

            // Use the engine as server
            engine.setUseClientMode(false);

            // Require client authentication
            engine.setNeedClientAuth(true);
            this.socket = new DatagramSocket(serverPort);

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
    public DataPacket receiveMessageAndSender() throws Exception {
        boolean endLoops = false;
        byte[] receiveBuffer = new byte[MAX_LEN];
        DatagramPacket datagram = new DatagramPacket(receiveBuffer, MAX_LEN);

        int loops = MAX_HANDSHAKE_LOOPS;
        engine.beginHandshake();
        while (!endLoops) {
            if (--loops < 0) {
                throw new RuntimeException("Too many loops to produce handshake packets");
            }
            SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
            if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                ByteBuffer iNet;
                ByteBuffer iApp;
                if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    // receive ClientHello request and other SSL/TLS/DTLS records
                    byte[] buf = new byte[1024];
                    datagram = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(datagram);
                    } catch (SocketTimeoutException ste) {
                        // retransmit the packet if timeout
//                        List <DatagramPacket> packets = onReceiveTimeout(engine, peerAddr);
//                        for (DatagramPacket p : packets) {
//                            socket.send(p);
//                        }
                        continue;
                    }
                    iNet = ByteBuffer.wrap(buf, 0, datagram.getLength());
                    iApp = ByteBuffer.allocate(1024);
                } else {
                    iNet = ByteBuffer.allocate(0);
                    iApp = ByteBuffer.allocate(1024);
                }
                SSLEngineResult r = engine.unwrap(iNet, iApp);
                SSLEngineResult.Status rs = r.getStatus();
                hs = r.getHandshakeStatus();
                if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    // the client maximum fragment size config does not work?
                    throw new Exception("Buffer overflow: " +
                            "incorrect client maximum fragment size");
                } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    // bad packet, or the client maximum fragment size
                    // config does not work?
                    if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                        throw new Exception("Buffer underflow: " +
                                "incorrect client maximum fragment size");
                    } // otherwise, ignore this packet
                } else if (rs == SSLEngineResult.Status.CLOSED) {
                    endLoops = true;
                }   // otherwise, SSLEngineResult.Status.OK:
                if (rs != SSLEngineResult.Status.OK) {
                    continue;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
//                List<DatagramPacket> packets =
//                        // Call a function to produce handshake packets
//                        produceHandshakePackets(engine, peerAddr);
//                for (DatagramPacket p : packets) {
//                    socket.send(p);
//                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
//                runDelegatedTasks(engine);
            } else if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                // OK, time to do application data exchange.
                endLoops = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                endLoops = true;
            }
        }
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Not ready for application data yet");
        }
        return new DataPacket(datagram.getAddress(), datagram.getPort(), new String(datagram.getData()));
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException {

    }
}

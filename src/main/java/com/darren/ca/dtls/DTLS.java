package com.darren.ca.dtls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static com.darren.ca.dtls.TLSProperties.KEYSTORE_PASSWORD;

public class DTLS {
    private static final Logger logger = LoggerFactory.getLogger(DTLS.class);
    private static int MAX_HANDSHAKE_LOOPS = 60;
    private static int BUFFER_SIZE = 65507;
    private static int MAXIMUM_PACKET_SIZE = 65507;
    private static int SOCKET_TIMEOUT = 10 * 1000; // in millis
    private final String trustFilename = this.getClass().getClassLoader().getResourceAsStream("truststore.jks").toString();
    private final String keyFilename = this.getClass().getClassLoader().getResourceAsStream("keystore.jks").toString();
    private static Exception clientException = null;
    private static Exception serverException = null;

    // run delegated tasks
    private static void runDelegatedTasks(SSLEngine engine) throws Exception {
        Runnable runnable;
        while ((runnable = engine.getDelegatedTask()) != null) {
            runnable.run();
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            throw new Exception("handshake shouldn't need additional tasks");
        }
    }

    private static void printHex(String prefix, byte[] bytes, int offset, int length) {
        HexDumpEncoder dump = new HexDumpEncoder();

        synchronized (System.out) {
            System.out.println(prefix);
            try {
                ByteBuffer bb = ByteBuffer.wrap(bytes, offset, length);
                dump.encodeBuffer(bb, System.out);
            } catch (Exception e) {
                // ignore
            }
            System.out.flush();
        }
    }

    private static void printHex(String prefix, ByteBuffer bb) {
        HexDumpEncoder dump = new HexDumpEncoder();

        synchronized (System.out) {
            System.out.println(prefix);
            try {
                dump.encodeBuffer(bb.slice(), System.out);
            } catch (Exception e) {
                // ignore
            }
            System.out.flush();
        }
    }

    public static DTLS createDTLS() {
        return new DTLS();
    }

    public SSLEngine createSSLEngine(boolean isClient) throws Exception {
        SSLContext context = getDTLSContext();
        SSLEngine engine = context.createSSLEngine();

        SSLParameters paras = engine.getSSLParameters();
        paras.setMaximumPacketSize(MAXIMUM_PACKET_SIZE);

        engine.setUseClientMode(isClient);
        engine.setSSLParameters(paras);

        return engine;
    }

    // get DTSL context
    private SSLContext getDTLSContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");

        char[] passphrase = KEYSTORE_PASSWORD.toCharArray();

        ks.load(this.getClass().getClassLoader().getResourceAsStream("keystore.jks"), passphrase);

        ts.load(this.getClass().getClassLoader().getResourceAsStream("truststore.jks"), passphrase);


        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ts);

        SSLContext sslCtx = SSLContext.getInstance("DTLS");

        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslCtx;
    }

    // receive application data
    public DTLSReceive receiveAppData(SSLEngine engine, DatagramSocket socket, ByteBuffer expectedApp)
            throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int MAX_APP_READ_LOOPS = 10;
        int loops = MAX_APP_READ_LOOPS;
        DTLSReceive dtlsReceive = new DTLSReceive();

        while ((serverException == null) && (clientException == null)) {
            if (--loops < 0) {
                throw new RuntimeException("Too much loops to receive application data");
            }
            socket.receive(packet);
            ByteBuffer netBuffer = ByteBuffer.wrap(buf, 0, packet.getLength());
            ByteBuffer recBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            SSLEngineResult rs = engine.unwrap(netBuffer, recBuffer);
            recBuffer.flip();
            if (recBuffer.remaining() != 0) {
                System.out.println(new String(recBuffer.array()));
                dtlsReceive.setByteBuffer(recBuffer);
                printHex("Received application data", recBuffer);
                if (!recBuffer.equals(recBuffer)) {
                    System.out.println("Engine status is " + rs);
                    throw new Exception("Not the right application data");
                }
                break;
            }
        }
        dtlsReceive.setDatagramPacket(packet);
        return dtlsReceive;
    }

    // handshake
    public void serverHandshake(SSLEngine engine, DatagramSocket socket, String side)
            throws Exception {
        SocketAddress peerAddr = null;
        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        engine.beginHandshake();
        while (!endLoops && (serverException == null) && (clientException == null)) {
            if (--loops < 0) {
                throw new RuntimeException("Too much loops to produce handshake packets");
            }

            SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
            logger.debug(side + "=======handshake(" + loops + ", " + hs + ")=======");
            if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ||
                    hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN) {
                logger.debug(side + "Receive DTLS records, handshake status is " + hs);
                ByteBuffer iNet;
                ByteBuffer iApp;
                if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                        peerAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());
                    } catch (SocketTimeoutException ste) {
                        logger.debug(side + "Warning: " + ste);

                        List<DatagramPacket> packets = new ArrayList<>();
                        boolean finished = onReceiveTimeout(engine, peerAddr, side, packets);

                        logger.debug(side + "Reproduced " + packets.size() + " packets");
                        for (DatagramPacket p : packets) {
                            printHex("Reproduced packet", p.getData(), p.getOffset(), p.getLength());
                            socket.send(p);
                        }
                        if (finished) {
                            logger.debug(side + "Handshake status is FINISHED " + "after calling onReceiveTimeout(), " + "finish the loop");
                            endLoops = true;
                        }
                        logger.debug(side + "New handshake status is " + engine.getHandshakeStatus());
                        continue;
                    }

                    iNet = ByteBuffer.wrap(buf, 0, packet.getLength());
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                } else {
                    iNet = ByteBuffer.allocate(0);
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                }

                SSLEngineResult r = engine.unwrap(iNet, iApp);
                SSLEngineResult.Status rs = r.getStatus();
                hs = r.getHandshakeStatus();
                if (rs == SSLEngineResult.Status.OK) {
                    // OK
                } else if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    logger.debug(side + "BUFFER_OVERFLOW, handshake status is " + hs);
                    // the client maximum fragment size config does not work?
                    throw new Exception("Buffer overflow: " + "incorrect client maximum fragment size");
                } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    logger.debug(side + "BUFFER_UNDERFLOW, handshake status is " + hs);

                    // bad packet, or the client maximum fragment size
                    // config does not work?
                    if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                        throw new Exception("Buffer underflow: " + "incorrect client maximum fragment size");
                    } // otherwise, ignore this packet
                } else if (rs == SSLEngineResult.Status.CLOSED) {
                    throw new Exception("SSL engine closed, handshake status is " + hs);
                } else {
                    throw new Exception("Can't reach here, result is " + rs);
                }

                if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                    logger.debug(side + "Handshake status is FINISHED, finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                List<DatagramPacket> packets = new ArrayList<>();
                boolean finished = produceHandshakePackets(engine, peerAddr, side, packets);
                logger.debug(side + "Produced " + packets.size() + " packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                if (finished) {
                    logger.debug(side + "Handshake status is FINISHED " + "after producing handshake packets, " + "finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks(engine);
            } else if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                logger.debug(side + "Handshake status is NOT_HANDSHAKING, finish the loop");
                endLoops = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                throw new Exception("Unexpected status, SSLEngine.getHandshakeStatus() " + "shouldn't return FINISHED");
            } else {
                throw new Exception("Can't reach here, handshake status is " + hs);
            }
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        logger.debug(side + "Handshake finished, status is " + hs);

        if (engine.getHandshakeSession() != null) {
            throw new Exception("Handshake finished, but handshake session is not null");
        }

        SSLSession session = engine.getSession();
        if (session == null) {
            throw new Exception("Handshake finished, but session is null");
        }
        logger.debug(side + "Negotiated protocol is " + session.getProtocol());
        logger.debug(side + "Negotiated cipher suite is " + session.getCipherSuite());
        // handshake status should be NOT_HANDSHAKING
        //
        // According to the spec, SSLEngine.getHandshakeStatus() can't
        // return FINISHED.
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Unexpected handshake status " + hs);
        }
    }

    // handshake
    public void handshake(SSLEngine engine, DatagramSocket socket, SocketAddress peerAddr, String side)
            throws Exception {

        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        engine.beginHandshake();
        while (!endLoops && (serverException == null) && (clientException == null)) {
            if (--loops < 0) {
                throw new RuntimeException("Too much loops to produce handshake packets");
            }

            SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
            logger.debug(side + "=======handshake(" + loops + ", " + hs + ")=======");
            if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ||
                    hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN) {
                logger.debug(side + "Receive DTLS records, handshake status is " + hs);
                ByteBuffer iNet;
                ByteBuffer iApp;
                if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException ste) {
                        logger.debug(side + "Warning: " + ste);

                        List<DatagramPacket> packets = new ArrayList<>();
                        boolean finished = onReceiveTimeout(engine, peerAddr, side, packets);

                        logger.debug(side + "Reproduced " + packets.size() + " packets");
                        for (DatagramPacket p : packets) {
                            printHex("Reproduced packet", p.getData(), p.getOffset(), p.getLength());
                            socket.send(p);
                        }
                        if (finished) {
                            logger.debug(side + "Handshake status is FINISHED " + "after calling onReceiveTimeout(), " + "finish the loop");
                            endLoops = true;
                        }
                        logger.debug(side + "New handshake status is " + engine.getHandshakeStatus());
                        continue;
                    }

                    iNet = ByteBuffer.wrap(buf, 0, packet.getLength());
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                } else {
                    iNet = ByteBuffer.allocate(0);
                    iApp = ByteBuffer.allocate(BUFFER_SIZE);
                }

                SSLEngineResult r = engine.unwrap(iNet, iApp);
                SSLEngineResult.Status rs = r.getStatus();
                hs = r.getHandshakeStatus();
                if (rs == SSLEngineResult.Status.OK) {
                    // OK
                } else if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                    logger.debug(side + "BUFFER_OVERFLOW, handshake status is " + hs);
                    // the client maximum fragment size config does not work?
                    throw new Exception("Buffer overflow: " + "incorrect client maximum fragment size");
                } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    logger.debug(side + "BUFFER_UNDERFLOW, handshake status is " + hs);

                    // bad packet, or the client maximum fragment size
                    // config does not work?
                    if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                        throw new Exception("Buffer underflow: " + "incorrect client maximum fragment size");
                    } // otherwise, ignore this packet
                } else if (rs == SSLEngineResult.Status.CLOSED) {
                    throw new Exception("SSL engine closed, handshake status is " + hs);
                } else {
                    throw new Exception("Can't reach here, result is " + rs);
                }

                if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                    logger.debug(side + "Handshake status is FINISHED, finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                List<DatagramPacket> packets = new ArrayList<>();
                boolean finished = produceHandshakePackets(engine, peerAddr, side, packets);
                logger.debug(side + "Produced " + packets.size() + " packets");
                for (DatagramPacket p : packets) {
                    socket.send(p);
                }

                if (finished) {
                    logger.debug(side + "Handshake status is FINISHED " + "after producing handshake packets, " + "finish the loop");
                    endLoops = true;
                }
            } else if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks(engine);
            } else if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                logger.debug(side + "Handshake status is NOT_HANDSHAKING, finish the loop");
                endLoops = true;
            } else if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                throw new Exception("Unexpected status, SSLEngine.getHandshakeStatus() " + "shouldn't return FINISHED");
            } else {
                throw new Exception("Can't reach here, handshake status is " + hs);
            }
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        logger.debug(side + "Handshake finished, status is " + hs);

        if (engine.getHandshakeSession() != null) {
            throw new Exception("Handshake finished, but handshake session is not null");
        }

        SSLSession session = engine.getSession();
        if (session == null) {
            throw new Exception("Handshake finished, but session is null");
        }
        logger.debug(side + "Negotiated protocol is " + session.getProtocol());
        logger.debug(side + "Negotiated cipher suite is " + session.getCipherSuite());
        // handshake status should be NOT_HANDSHAKING
        //
        // According to the spec, SSLEngine.getHandshakeStatus() can't
        // return FINISHED.
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Unexpected handshake status " + hs);
        }
    }

    // deliver application data
    public void deliverAppData(SSLEngine engine, DatagramSocket socket, ByteBuffer appData, SocketAddress peerAddr)
            throws Exception {
        // Note: have not consider the packet loses
        List<DatagramPacket> packets = produceApplicationPackets(engine, appData, peerAddr);
        appData.flip();
        for (DatagramPacket p : packets) {
            socket.send(p);
        }
    }

    // produce application packets
    private List<DatagramPacket> produceApplicationPackets(SSLEngine engine, ByteBuffer source, SocketAddress socketAddr)
            throws Exception {
        List<DatagramPacket> packets = new ArrayList<>();
        ByteBuffer appNet = ByteBuffer.allocate(MAXIMUM_PACKET_SIZE);
        SSLEngineResult r = engine.wrap(source, appNet);
        appNet.flip();

        SSLEngineResult.Status rs = r.getStatus();
        if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            // the client maximum fragment size config does not work?
            throw new Exception("Buffer overflow: " + "incorrect server maximum fragment size");
        } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
            // unlikely
            throw new Exception("Buffer underflow during wraping");
        } else if (rs == SSLEngineResult.Status.CLOSED) {
            throw new Exception("SSLEngine has closed");
        } else if (rs == SSLEngineResult.Status.OK) {
            // OK
        } else {
            throw new Exception("Can't reach here, result is " + rs);
        }

        // SSLEngineResult.Status.OK:
        if (appNet.hasRemaining()) {
            byte[] ba = new byte[appNet.remaining()];
            appNet.get(ba);
            DatagramPacket packet = new DatagramPacket(ba, ba.length, socketAddr);
            packets.add(packet);
        }

        return packets;
    }

    // produce handshake packets
    private boolean produceHandshakePackets(SSLEngine engine, SocketAddress socketAddr, String side, List<DatagramPacket> packets)
            throws Exception {
        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS / 2;
        while (!endLoops && (serverException == null) && (clientException == null)) {
            if (--loops < 0) {
                throw new RuntimeException("Too much loops to produce handshake packets");
            }

            ByteBuffer oNet = ByteBuffer.allocate(MAXIMUM_PACKET_SIZE);
            ByteBuffer oApp = ByteBuffer.allocate(0);
            SSLEngineResult r = engine.wrap(oApp, oNet);
            oNet.flip();

            SSLEngineResult.Status rs = r.getStatus();
            SSLEngineResult.HandshakeStatus hs = r.getHandshakeStatus();
            logger.debug(side + "----produce handshake packet(" + loops + ", " + rs + ", " + hs + ")----");
            if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                // the client maximum fragment size config does not work?
                throw new Exception("Buffer overflow: " + "incorrect server maximum fragment size");
            } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                logger.debug(side + "Produce handshake packets: BUFFER_UNDERFLOW occured");
                logger.debug(side + "Produce handshake packets: Handshake status: " + hs);
                // bad packet, or the client maximum fragment size
                // config does not work?
                if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                    throw new Exception("Buffer underflow: " + "incorrect server maximum fragment size");
                } // otherwise, ignore this packet
            } else if (rs == SSLEngineResult.Status.CLOSED) {
                throw new Exception("SSLEngine has closed");
            } else if (rs == SSLEngineResult.Status.OK) {
                // OK
            } else {
                throw new Exception("Can't reach here, result is " + rs);
            }

            // SSLEngineResult.Status.OK:
            if (oNet.hasRemaining()) {
                byte[] ba = new byte[oNet.remaining()];
                oNet.get(ba);
                DatagramPacket packet = createHandshakePacket(ba, socketAddr);
                packets.add(packet);
            }

            if (hs == SSLEngineResult.HandshakeStatus.FINISHED) {
                logger.debug(side + "Produce handshake packets: " + "Handshake status is FINISHED, finish the loop");
                return true;
            }

            boolean endInnerLoop = false;
            SSLEngineResult.HandshakeStatus nhs = hs;
            while (!endInnerLoop) {
                if (nhs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks(engine);
                } else if (nhs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP ||
                        nhs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN ||
                        nhs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {

                    endInnerLoop = true;
                    endLoops = true;
                } else if (nhs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    endInnerLoop = true;
                } else if (nhs == SSLEngineResult.HandshakeStatus.FINISHED) {
                    throw new Exception("Unexpected status, SSLEngine.getHandshakeStatus() " + "shouldn't return FINISHED");
                } else {
                    throw new Exception("Can't reach here, handshake status is " + nhs);
                }
                nhs = engine.getHandshakeStatus();
            }
        }

        return false;
    }

    private DatagramPacket createHandshakePacket(byte[] ba, SocketAddress socketAddr) {
        return new DatagramPacket(ba, ba.length, socketAddr);
    }

    // retransmission if timeout
    private boolean onReceiveTimeout(SSLEngine engine, SocketAddress socketAddr, String side, List<DatagramPacket> packets) throws Exception {
        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            return false;
        } else {
            // retransmission of handshake messages
            return produceHandshakePackets(engine, socketAddr, side, packets);
        }
    }
}

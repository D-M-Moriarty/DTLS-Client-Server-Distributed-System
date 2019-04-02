package com.darren.ca.server.payload;

import com.darren.ca.client.request.HexDumpEncoder;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_PASSWORD;

public class DTLS2 {

    public static DatagramSocket serverDatagramSocket = null;
    public static SocketAddress clientSocketAddr = null;
    /*
     * The following is to set up the keystores.
     */
    private static String pathToStores = "D:\\LEL";
    private static String keyStoreFile = "keystore.jks";
    private static String passwd = "123456";
    private static int MAX_HANDSHAKE_LOOPS = 60;
    private static int MAX_APP_READ_LOOPS = 10;
    private static Exception serverException = null;

    private static ByteBuffer serverApp
            = ByteBuffer.wrap("Hi Client, I'm Server".getBytes());
    private static ByteBuffer clientApp
            = ByteBuffer.wrap("Hi Server, I'm Client".getBytes());
    private static final String trustFilename = "/Users/darrenmoriarty/Desktop/distributedcomputing/src/main/java/com/darren/ca/server/tls/truststore.jks";
    private static final String keyFilename = "/Users/darrenmoriarty/Development/TestDTLS/src/things/keystore.jks";

    private String keystoreName = trustFilename;

    public static void main(String[] args) throws Exception {

        serverDatagramSocket = new DatagramSocket(9110);

        DTLS2 dTLSServer = new DTLS2();

        dTLSServer.Process(dTLSServer);
    }

    public void Process(DTLS2 dTLSServer) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<String>> list = new ArrayList<>();

        try {
            list.add(pool.submit(new ServerCallable(dTLSServer)));  // server task
        } finally {
            pool.shutdown();
        }

        Exception reserved = null;
        for (Future<String> fut : list) {
            try {
                System.out.println(fut.get());
            } catch (CancellationException
                    | InterruptedException | ExecutionException cie) {
                if (reserved != null) {
                    cie.addSuppressed(reserved);
                    reserved = cie;
                } else {
                    reserved = cie;
                }
            }
        }

        if (reserved != null) {
            throw reserved;
        }
    }

    /*
     * Define the server side of the test.
     */
    void doServerSide() throws Exception {
        DatagramSocket socket = serverDatagramSocket;
        socket.setSoTimeout(10000);   // 10 second

        // create SSLEngine
        SSLEngine engine = createSSLEngine(false);

        // handshaking
        handshake(engine, socket, clientSocketAddr);

        // read client application data
        receiveAppData(engine, socket, clientApp);

        // write server application data
        deliverAppData(engine, socket, serverApp, clientSocketAddr);

        socket.close();
    }

    /*
     * =============================================================
     * The remainder is support stuff for DTLS operations.
     */
    SSLEngine createSSLEngine(boolean isClient) throws Exception {
        SSLContext context = getDTLSContext();
        SSLEngine engine = context.createSSLEngine();

        SSLParameters paras = engine.getSSLParameters();
//        paras.setMaximumPacketSize(1024);

        engine.setUseClientMode(isClient);
        engine.setSSLParameters(paras);

        return engine;
    }

    // get DTSL context
    SSLContext getDTLSContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
//        KeyStore ts = KeyStore.getInstance("JKS");

        char[] passphrase = KEYSTORE_PASSWORD.toCharArray();

        ks.load(new FileInputStream(keystoreName), passphrase);
//        ts.load(new FileInputStream(trustFilename), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sslCtx = SSLContext.getInstance("DTLS");

        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslCtx;
    }

    // Will the handshaking and application data exchange succeed?
    public boolean isGoodJob() {
        return true;
    }

    // handshake
    void handshake(SSLEngine engine, DatagramSocket socket, SocketAddress peerAddr) throws Exception {

        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        engine.beginHandshake();
        while (!endLoops
                && (serverException == null)) {

            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to produce handshake packets");
            }

            SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
            if (null != hs) {
                switch (hs) {
                    case NEED_UNWRAP:
//                    case NEED_UNWRAP_AGAIN:
                        ByteBuffer iNet;
                        ByteBuffer iApp;
                        if (hs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                            // receive ClientHello request and other SSL/TLS records
                            byte[] buf = new byte[1024];
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            try {
                                socket.receive(packet);
                                clientSocketAddr = new InetSocketAddress(packet.getAddress(), packet.getPort());
                                peerAddr = clientSocketAddr;
                            } catch (SocketTimeoutException ste) {

                                List<DatagramPacket> packets = onReceiveTimeout(engine, peerAddr);
                                for (DatagramPacket p : packets) {
                                    socket.send(p);
                                }

                                continue;
                            }

                            iNet = ByteBuffer.wrap(buf, 0, packet.getLength());
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
                            throw new Exception("Buffer overflow: "
                                    + "incorrect client maximum fragment size");
                        } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                            // bad packet, or the client maximum fragment size
                            // config does not work?
                            if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                                throw new Exception("Buffer underflow: "
                                        + "incorrect client maximum fragment size");
                            } // otherwise, ignore this packet
                        } else if (rs == SSLEngineResult.Status.CLOSED) {
                            endLoops = true;
                        }   // otherwise, SSLEngineResult.Status.OK:
                        if (rs != SSLEngineResult.Status.OK) {
                            continue;
                        }
                        break;
                    case NEED_WRAP:
                        List<DatagramPacket> packets
                                = produceHandshakePackets(engine, peerAddr);
                        for (DatagramPacket p : packets) {
                            socket.send(p);
                        }
                        break;
                    case NEED_TASK:
                        runDelegatedTasks(engine);
                        break;
                    case NOT_HANDSHAKING:
                        // OK, time to do application data exchange.
                        endLoops = true;
                        break;
                    case FINISHED:
                        endLoops = true;
                        break;
                    default:
                        break;
                }
            }
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            throw new Exception("Not ready for application data yet");
        }
    }

    // deliver application data
    void deliverAppData(SSLEngine engine, DatagramSocket socket, ByteBuffer appData, SocketAddress peerAddr) throws Exception {

        // Note: have not consider the packet loses
        List<DatagramPacket> packets = produceApplicationPackets(engine, appData, peerAddr);
        appData.flip();
        for (DatagramPacket p : packets) {
            socket.send(p);
        }
    }

    // receive application data
    void receiveAppData(SSLEngine engine, DatagramSocket socket, ByteBuffer expectedApp) throws Exception {

        int loops = MAX_APP_READ_LOOPS;
        while ((serverException == null)) {
            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to receive application data");
            }

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            ByteBuffer netBuffer = ByteBuffer.wrap(buf, 0, packet.getLength());
            ByteBuffer recBuffer = ByteBuffer.allocate(1024);
            SSLEngineResult rs = engine.unwrap(netBuffer, recBuffer);
            recBuffer.flip();
            if (recBuffer.remaining() != 0) {
                printHex("Received application data", recBuffer);
                if (!recBuffer.equals(expectedApp)) {
                    System.out.println("Engine status is " + rs);
                    throw new Exception("Not the right application data");
                }
                break;
            }
        }
    }

    // produce handshake packets
    List<DatagramPacket> produceHandshakePackets(
            SSLEngine engine, SocketAddress socketAddr) throws Exception {

        List<DatagramPacket> packets = new ArrayList<>();
        boolean endLoops = false;
        int loops = MAX_HANDSHAKE_LOOPS;
        while (!endLoops
                && (serverException == null)) {

            if (--loops < 0) {
                throw new RuntimeException(
                        "Too much loops to produce handshake packets");
            }

            ByteBuffer oNet = ByteBuffer.allocate(32768);
            ByteBuffer oApp = ByteBuffer.allocate(0);
            SSLEngineResult r = engine.wrap(oApp, oNet);
            oNet.flip();

            SSLEngineResult.Status rs = r.getStatus();
            SSLEngineResult.HandshakeStatus hs = r.getHandshakeStatus();
            if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                // the client maximum fragment size config does not work?
                throw new Exception("Buffer overflow: "
                        + "incorrect server maximum fragment size");
            } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                // bad packet, or the client maximum fragment size
                // config does not work?
                if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                    throw new Exception("Buffer underflow: "
                            + "incorrect server maximum fragment size");
                } // otherwise, ignore this packet
            } else if (rs == SSLEngineResult.Status.CLOSED) {
                throw new Exception("SSLEngine has closed");
            }   // otherwise, SSLEngineResult.Status.OK

            // SSLEngineResult.Status.OK:
            if (oNet.hasRemaining()) {
                byte[] ba = new byte[oNet.remaining()];
                oNet.get(ba);
                DatagramPacket packet = createHandshakePacket(ba, socketAddr);
                packets.add(packet);
            }

            boolean endInnerLoop = false;
            SSLEngineResult.HandshakeStatus nhs = hs;
            while (!endInnerLoop) {
                if (nhs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks(engine);
                    nhs = engine.getHandshakeStatus();
                } else if ((nhs == SSLEngineResult.HandshakeStatus.FINISHED)
                        || (nhs == SSLEngineResult.HandshakeStatus.NEED_UNWRAP)
                        || (nhs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)) {

                    endInnerLoop = true;
                    endLoops = true;
                } else if (nhs == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
                    endInnerLoop = true;
                }
            }
        }

        return packets;
    }

    DatagramPacket createHandshakePacket(byte[] ba, SocketAddress socketAddr) {
        return new DatagramPacket(ba, ba.length, socketAddr);
    }

    // produce application packets
    List<DatagramPacket> produceApplicationPackets(
            SSLEngine engine, ByteBuffer source,
            SocketAddress socketAddr) throws Exception {

        List<DatagramPacket> packets = new ArrayList<>();
        ByteBuffer appNet = ByteBuffer.allocate(32768);
        SSLEngineResult r = engine.wrap(source, appNet);
        appNet.flip();

        SSLEngineResult.Status rs = r.getStatus();
        if (rs == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            // the client maximum fragment size config does not work?
            throw new Exception("Buffer overflow: "
                    + "incorrect server maximum fragment size");
        } else if (rs == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
            // unlikely
            throw new Exception("Buffer underflow during wraping");
        } else if (rs == SSLEngineResult.Status.CLOSED) {
            throw new Exception("SSLEngine has closed");
        }   // otherwise, SSLEngineResult.Status.OK

        // SSLEngineResult.Status.OK:
        if (appNet.hasRemaining()) {
            byte[] ba = new byte[appNet.remaining()];
            appNet.get(ba);
            DatagramPacket packet
                    = new DatagramPacket(ba, ba.length, socketAddr);
            packets.add(packet);
        }

        return packets;
    }

    // run delegated tasks
    void runDelegatedTasks(SSLEngine engine) throws Exception {
        Runnable runnable;
        while ((runnable = engine.getDelegatedTask()) != null) {
            runnable.run();
        }

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            throw new Exception("handshake shouldn't need additional tasks");
        }
    }

    // retransmission if timeout
    List<DatagramPacket> onReceiveTimeout(SSLEngine engine, SocketAddress socketAddr) throws Exception {

        SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
        if (hs == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            return new ArrayList<DatagramPacket>();
        } else {
            // retransmission of handshake messages
            return produceHandshakePackets(engine, socketAddr);
        }
    }

    void printHex(String prefix, ByteBuffer bb) {
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

    void printHex(String prefix, byte[] bytes, int offset, int length) {

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

    class ServerCallable implements Callable<String> {

        DTLS2 dtlsServer;

        ServerCallable(DTLS2 testCase) {
            this.dtlsServer = testCase;
        }

        @Override
        public String call() throws Exception {
            try {
                dtlsServer.doServerSide();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                serverException = e;

                if (dtlsServer.isGoodJob()) {
                    throw e;
                } else {
                    return "Well done, server!";
                }
            } finally {
                if (serverDatagramSocket != null) {
                    serverDatagramSocket.close();
                }
            }

            if (dtlsServer.isGoodJob()) {
                return "Well done, server!";
            } else {
                throw new Exception("No expected exception");
            }
        }
    }

}

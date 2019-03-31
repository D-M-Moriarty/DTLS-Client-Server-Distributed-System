package com.darren.ca.server.bouncycastle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.DatagramSocket;
import java.security.KeyStore;

import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_PASSWORD;

public class ServerTestMobius {
    private static final String keystorePassword = "GfUNokaofNh6";
    private static final String keystorePassword2 = "111111";
    private static final String clientKeystorePassword = "qwe321";
    private static final String clientKeystorePassword2 = "111111";
    private DatagramSocket datagramSocket;
    private String keystoreName = KEYSTORE_NAME;
    private char[] ctPass = KEYSTORE_PASSWORD.toCharArray();
    private KeyStore keyStoreKeys;
    private KeyStore keyStoreTrust;

    public ServerTestMobius() throws Exception {
        KeyStore testKeystore = null;
        testKeystore = KeyStore.getInstance("JKS");
        testKeystore.load(this.getClass().getClassLoader().getResourceAsStream("dtls-demo.jks"), keystorePassword.toCharArray());

        KeyStore clientKeystore = null;
        clientKeystore = KeyStore.getInstance("PKCS12");
        clientKeystore.load(this.getClass().getClassLoader().getResourceAsStream("p.pfx"), clientKeystorePassword.toCharArray());

        DtlsServer server = new DtlsServer("localhost", 5555, testKeystore, keystorePassword, null);
        server.initServer();

        byte[] test2Bytes = "server hello message".getBytes();
        ByteBuf buffer2 = Unpooled.wrappedBuffer(test2Bytes);


    }

    public static void main(String[] args) throws Exception {
        new ServerTestMobius();
    }
}

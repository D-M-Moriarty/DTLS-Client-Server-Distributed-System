package com.darren.ca.server.bouncycastle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.net.DatagramSocket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_PASSWORD;

public class ClientTestMobius {

    private static final String keystorePassword = "GfUNokaofNh6";
    private static final String keystorePassword2 = "111111";
    private static final String clientKeystorePassword = "qwe321";
    private static final String clientKeystorePassword2 = "111111";
    private DatagramSocket datagramSocket;
    private String keystoreName = KEYSTORE_NAME;
    private char[] ctPass = KEYSTORE_PASSWORD.toCharArray();
    private KeyStore keyStoreKeys;
    private KeyStore keyStoreTrust;

    public ClientTestMobius() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore testKeystore = null;
        testKeystore = KeyStore.getInstance("JKS");
        testKeystore.load(this.getClass().getClassLoader().getResourceAsStream("dtls-demo.jks"), keystorePassword.toCharArray());

        KeyStore clientKeystore = null;
        clientKeystore = KeyStore.getInstance("PKCS12");
        clientKeystore.load(this.getClass().getClassLoader().getResourceAsStream("p.pfx"), clientKeystorePassword.toCharArray());

        DtlsClient client = new DtlsClient("localhost", "localhost", 5555, clientKeystore, clientKeystorePassword);
        client.establishConnection();

        byte[] testBytes = "hello message from darren".getBytes();
        ByteBuf buffer = Unpooled.wrappedBuffer(testBytes);

        client.sendPacket(buffer);
    }

    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        new ClientTestMobius();
    }
}

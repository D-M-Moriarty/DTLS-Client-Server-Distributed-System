package com.darren.ca.server.payload;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.server.tls.TLSProperties.KEYSTORE_PASSWORD;

public class DTLSServer {
    private char[] keystorePassword = KEYSTORE_PASSWORD.toCharArray();
    private String keystoreName = KEYSTORE_NAME;
    private char[] ctPass = KEYSTORE_PASSWORD.toCharArray();
    private KeyStore keyStore;
    private KeyManagerFactory keyManagerFactory;
    private SSLContext sslContext;
    private SSLServerSocketFactory sslServerSocketFactory;
    private SSLServerSocket serverSSLSocket;
    private SSLSocket sslSocket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public DTLSServer() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        // Create and initialize the SSLContext with key material
        char[] passphrase = "passphrase".toCharArray();

        // First initialize the key and trust material
        KeyStore ksKeys = KeyStore.getInstance("PKCS12");
        ksKeys.load(new FileInputStream("testKeys"), passphrase);
        KeyStore ksTrust = KeyStore.getInstance("PKCS12");
        ksTrust.load(new FileInputStream("testTrust"), passphrase);

        // KeyManagers decide which key material to use
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(ksKeys, passphrase);

        // TrustManagers decide whether to allow connections
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ksTrust);

        // Get an SSLContext for DTLS Protocol without authentication
        sslContext = SSLContext.getInstance("DTLS");
        sslContext.init(null, null, null);

        // Create the engine
        SSLEngine engine = sslContext.createSSLEngine("hostname", 3000);

        // Use the engine as server
        engine.setUseClientMode(false);

        // Require client authentication
        engine.setNeedClientAuth(true);
    }

}

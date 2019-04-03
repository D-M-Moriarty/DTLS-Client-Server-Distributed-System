package com.darren.ca.server.payload;

import com.darren.ca.server.model.DataPacket;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_NAME;
import static com.darren.ca.client.tls.TLSProperties.KEYSTORE_PASSWORD;

public class ServerSSLSocket implements ServerSocketDatagram {
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

    public ServerSSLSocket(int port) {
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystoreName), keystorePassword);
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, ctPass);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            sslServerSocketFactory = sslContext.getServerSocketFactory();
            serverSSLSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
            sslSocket = (SSLSocket) serverSSLSocket.accept();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }


    @Override
    public DataPacket receiveMessageAndSender() throws IOException {
        reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        String data = reader.readLine();
//        reader.close();
        return new DataPacket(InetAddress.getLocalHost(), 4323, data);
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
        writer.write("Hi from server", 0, "Hi from server".length());
        writer.flush();
//        writer.close();
    }
}

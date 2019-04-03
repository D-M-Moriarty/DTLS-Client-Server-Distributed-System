package com.darren.ca.server.payload;

import com.darren.ca.server.model.DataPacket;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.CertificateException;

import static com.darren.ca.dtls.TLSProperties.KEYSTORE_PASSWORD;

public class ServerSSLSocket implements ServerSocketDatagram {
    private SSLSocket sslSocket;

    public ServerSSLSocket(int port) {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            char[] keystorePassword = KEYSTORE_PASSWORD.toCharArray();
            String keystoreName = "Name";
            keyStore.load(new FileInputStream(keystoreName), keystorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            char[] ctPass = KEYSTORE_PASSWORD.toCharArray();
            keyManagerFactory.init(keyStore, ctPass);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSSLSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        String data = reader.readLine();
//        reader.close();
        return new DataPacket(InetAddress.getLocalHost(), 4323, data);
    }

    @Override
    public void sendFile(InetAddress receiverHost, int receiverPort, byte[] file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
        writer.write("Hi from server", 0, "Hi from server".length());
        writer.flush();
//        writer.close();
    }
}

package com.darren.ca.client.request;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;

import static com.darren.ca.server.constants.ServerProperties.SERVER_ADDRESS;
import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

public class ClientSSLSocket implements ClientSocketDatagram {
    private BufferedWriter writer;
    private BufferedReader reader;
    private SSLSocket sslSocket;

    public ClientSSLSocket() {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            // TODO remove the hardcoded server properties
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(SERVER_ADDRESS, SERVER_PORT);
            sslSocket.startHandshake();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDatagramMessage(InetAddress receiverHost, int receiverPort, String message) throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
        writer.write(message, 0, message.length());
        writer.newLine();
        writer.flush();
//        writer.close();
    }

    @Override
    public void sendDatagramPacket(InetAddress receiverHost, int receiverPort, byte[] data) throws IOException {
        sendDatagramMessage(receiverHost, receiverPort, data.toString());
    }

    @Override
    public String receiveServerResponse() throws IOException {
        reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        String f = reader.readLine();
        return f;
    }

    @Override
    public void closeSocket() {

    }
}

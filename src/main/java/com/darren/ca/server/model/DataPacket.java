package com.darren.ca.server.model;

import java.net.InetAddress;

import static com.darren.ca.server.constants.ServerProperties.OPERATION_INDEX;

public class DataPacket {
    private InetAddress host;
    private int port;
    private short operationCode;
    private String message;
    private String payload;

    public DataPacket(InetAddress host, int port, String message) {
        this.host = host;
        this.port = port;
        this.message = message;
        setOperationCode(message);
        setPayload(message);
    }

    public InetAddress getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMessage() {
        return message;
    }

    public short getOperationCode() {
        return operationCode;
    }

    private void setOperationCode(String message) {
        String op = message.substring(0, OPERATION_INDEX);
        operationCode = Short.parseShort(op);
    }

    public String getPayload() {
        return payload;
    }

    private void setPayload(String message) {
        payload = message.substring(OPERATION_INDEX);
    }
}

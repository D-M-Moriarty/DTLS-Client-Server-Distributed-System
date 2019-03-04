package com.darren.ca.server.model;

import com.darren.ca.server.constants.RequestProtocol;
import com.darren.ca.server.constants.ServerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataPacketTest {

    private DataPacket dataPacket;
    private String loginString = RequestProtocol.LOGIN + "darren123";

    @BeforeEach
    void setUp() {
        try {
            dataPacket = new DataPacket(
                    InetAddress.getByName(ServerProperties.SERVER_ADDRESS),
                    ServerProperties.SERVER_PORT,
                    loginString);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void setOperationCode() {
        short operationCode = dataPacket.getOperationCode();
        assertEquals(100, operationCode);
    }

    @Test
    void setPayload() {
        String payload = dataPacket.getPayload();
        assertEquals("darren123", payload);
    }
}
package com.darren.ca.dtls;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class DTLSReceive {
    private ByteBuffer byteBuffer;
    private DatagramPacket datagramPacket;

    public byte[] getBufferBytes() {
        return byteBuffer.array();
    }

    void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }
}

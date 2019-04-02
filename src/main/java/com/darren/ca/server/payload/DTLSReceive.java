package com.darren.ca.server.payload;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class DTLSReceive {
    private ByteBuffer byteBuffer;
    private DatagramPacket datagramPacket;

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    public void setDatagramPacket(DatagramPacket datagramPacket) {
        this.datagramPacket = datagramPacket;
    }
}

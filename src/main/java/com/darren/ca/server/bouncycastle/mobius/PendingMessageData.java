package com.darren.ca.server.bouncycastle.mobius;

/**
 * Mobius Software LTD
 * Copyright 2018, Mobius Software LTD
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import io.netty.buffer.ByteBuf;

public class PendingMessageData {
    private ByteBuf buffer;
    private Integer wrottenBytes;

    public PendingMessageData(ByteBuf buffer) {
        this.buffer = buffer;
        this.wrottenBytes = 0;
    }

    public void writeBytes(ByteBuf data, int position) {
        int bytesToWrite = data.readableBytes();
        buffer.writerIndex(position);
        buffer.writeBytes(data);
        wrottenBytes += bytesToWrite;
    }

    public Integer getWrottenBytes() {
        return wrottenBytes;
    }

    public ByteBuf getBuffer() {
        return buffer;
    }
}
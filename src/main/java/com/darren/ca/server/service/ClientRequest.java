package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.payload.Response;

public interface ClientRequest {
    Response handleRequest(DataPacket requestDataPacket);
}

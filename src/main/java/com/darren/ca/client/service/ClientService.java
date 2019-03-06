package com.darren.ca.client.service;

import java.io.IOException;

public interface ClientService {
    String sendClientRequest(String request);

    String sendFileData(byte[] fileBytes) throws IOException;

    void closeDatagramSocket();
}

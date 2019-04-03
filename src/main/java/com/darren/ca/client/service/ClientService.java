package com.darren.ca.client.service;

public interface ClientService {
    String sendClientRequest(String request);

    String sendFileData(byte[] fileBytes);

    void closeDatagramSocket();
}

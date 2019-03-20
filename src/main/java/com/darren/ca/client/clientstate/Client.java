package com.darren.ca.client.clientstate;

public interface Client {
    void login(String username, String password);

    void logout(String username, String password);

    void uploadFile();

    void downloadFile();
}

package com.darren.ca.client;

public interface Client {
    short login(String username, String password);

    short logout(String username, String password);

    short uploadFile();

    short downloadFile();
}

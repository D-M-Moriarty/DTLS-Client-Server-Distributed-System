package com.darren.ca.client;

import java.io.File;

public interface Client {
    short login(String username, String password);

    short logout(String username, String password);

    short uploadFile(File jFrame);

    short downloadFile();
}

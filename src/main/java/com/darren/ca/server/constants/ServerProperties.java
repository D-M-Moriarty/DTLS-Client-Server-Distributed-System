package com.darren.ca.server.constants;

import java.io.File;

public final class ServerProperties {
    public static final int SERVER_PORT = 3000;
    public static final String SERVER_ADDRESS = "localhost";
    public static final int OPERATION_INDEX = 3;
    private static final String OUTPUT_DESTINATION = String.valueOf(new File(System.getProperty("user.home"), "/Desktop"));
    private static final String DESTINATION_FOLDER = "/TEST_FOLDER_FTP/";
    public static final String CLIENT_DESTINATION = OUTPUT_DESTINATION + DESTINATION_FOLDER;

    private ServerProperties() {
    }
}


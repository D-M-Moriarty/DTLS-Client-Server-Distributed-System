package com.darren.ca.client.constants;

import java.io.File;

public final class ClientStrings {
    public static final String LOGINREQUIRED = "You must login to do that";
    public static final String DESKTOP_DEST = new File(System.getProperty("user.home"), "/Desktop") + "/ClientFolders/";
    public static final String FOLDER_EXT = "_folder/";

    private ClientStrings() {
    }
}

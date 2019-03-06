package com.darren.ca.client.constants;

public final class ServerResponse {
    public static final short UNKNOWN_USER = 101;
    public static final short VALID_USER_INVALID_PASSWORD = 102;
    public static final short SUCCESSFUL_LOGIN = 103;
    public static final short USER_ALREADY_LOGGED_IN = 104;
    public static final short USER_LOGGED_OUT = 105;
    public static final short USER_NOT_LOGGED_IN = 106;
    public static final short FILE_UPLOAD_SUCCESSFUL = 107;
    public static final short FILE_UPLOAD_FAILED = 108;
    public static final short FILE_DOWNLOAD_SUCCESS = 109;
    public static final short FILE_DOWNLOAD_FAILED = 110;

    private ServerResponse() {
    }
}

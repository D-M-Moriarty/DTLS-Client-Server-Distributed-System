package com.darren.ca.client.constants;

public final class ServerResponse {
    public static final short UNKNOWN_USER = 101;
    public static final short VALID_USER_INVALID_PASSWORD = 102;
    public static final short SUCCESSFUL_LOGIN = 103;
    public static final short USER_ALREADY_LOGGED_IN = 104;

    private ServerResponse() {
    }
}

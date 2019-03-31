package com.darren.ca.server.model;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public class LoggedInUser {
    private User user;
    private int clientPortNum;
    private InetAddress clientIP;

    public LoggedInUser(User user, @NotNull DataPacket dataPacket) {
        this.user = user;
        this.clientIP = dataPacket.getHost();
        this.clientPortNum = dataPacket.getPort();
    }

    private LoggedInUser() {
    }

    @NotNull
    @Contract(" -> new")
    static LoggedInUser getNullUser() {
        return new LoggedInUser();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getPassword() {
        return user.getPassword();
    }

    User getUser() {
        return user;
    }

    int getClientPortNum() {
        return clientPortNum;
    }

    InetAddress getClientIP() {
        return clientIP;
    }

    @Override
    public String toString() {
        return "LoggedInUser{" +
                "user=" + user +
                ", clientPortNum=" + clientPortNum +
                ", clientIP=" + clientIP +
                '}';
    }
}

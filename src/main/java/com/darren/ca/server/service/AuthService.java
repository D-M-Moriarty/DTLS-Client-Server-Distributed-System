package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.RegisteredUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;

import static com.darren.ca.client.constants.ServerResponse.UNKNOWN_USER;
import static com.darren.ca.server.utils.Regex.extractPassword;
import static com.darren.ca.server.utils.Regex.extractUsername;

abstract class AuthService implements ClientRequest {
    int responseCode = UNKNOWN_USER;
    DataPacket dataPacket;

    public Response handleRequest(DataPacket requestDataPacket) {
        dataPacket = requestDataPacket;
        String username = extractUsername(requestDataPacket.getPayload());
        String password = extractPassword(requestDataPacket.getPayload());

        userAction(username, password);
        Response response = Response.initializeResponse();
        response.setResponseCode(responseCode);
        return response;
    }

    protected abstract void userAction(String username, String password);

    User findByUsername(String username) {
        return RegisteredUsers.checkForRegisteredUser(username);
    }
}

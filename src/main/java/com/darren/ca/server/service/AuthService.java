package com.darren.ca.server.service;

import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.model.RegisteredUsers;
import com.darren.ca.server.model.User;
import com.darren.ca.server.payload.Response;

import static com.darren.ca.client.constants.ServerResponse.UNKNOWN_USER;
import static com.darren.ca.server.utils.Regex.extractPassword;
import static com.darren.ca.server.utils.Regex.extractUsername;

abstract class AuthService implements ClientRequest {
    //    initial response
    int responseCode = UNKNOWN_USER;
    DataPacket dataPacket;

    public Response handleRequest(DataPacket requestDataPacket) {
//        incoming data packet
        dataPacket = requestDataPacket;
//        extract username and password with regex methods
        String username = extractUsername(requestDataPacket.getPayload());
        String password = extractPassword(requestDataPacket.getPayload());
//      userAction is an abstract method that is implemented in both of the subclasses
//      this is also a template method pattern
        userAction(username, password);
//         create a response object
        Response response = Response.initializeResponse();
//        set the response code
        response.setResponseCode(responseCode);
//         return the response to the client
        return response;
    }

    protected abstract void userAction(String username, String password);

    //  find a user by their username
    User findByUsername(String username) {
        return RegisteredUsers.checkForRegisteredUser(username);
    }
}

package com.darren.ca.server.factory;

import com.darren.ca.server.ClientRequest;

public abstract class RequestFactory {
    protected abstract ClientRequest createClientRequest(String request);

    public ClientRequest getClientRequest(String request) {
        return createClientRequest(request);
    }
}

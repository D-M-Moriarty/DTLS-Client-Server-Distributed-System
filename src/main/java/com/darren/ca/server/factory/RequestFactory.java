package com.darren.ca.server.factory;

import com.darren.ca.server.service.ClientRequest;

public abstract class RequestFactory {
    protected abstract ClientRequest createClientRequest(short request);

    public ClientRequest getClientRequest(short request) {
        return createClientRequest(request);
    }
}

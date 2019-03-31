package com.darren.ca.server.factory;

import com.darren.ca.server.service.ClientRequest;

public abstract class RequestFactory {
    //    factory method
    protected abstract ClientRequest createClientRequest(short request);

    //  calls factory method
    public ClientRequest getClientRequest(short request) {
        return createClientRequest(request);
    }
}

package com.darren.ca.server.factory;

import com.darren.ca.server.ClientRequest;
import com.darren.ca.server.service.FileDownloadService;
import com.darren.ca.server.service.FileUploadService;
import com.darren.ca.server.service.LoginService;
import com.darren.ca.server.service.LogoutService;

public class StandardClientRequest extends RequestFactory {
    protected ClientRequest createClientRequest(String request) {
        if ("login".equals(request)) {
            return new LoginService();
        } else if ("upload".equals(request)) {
            return new FileUploadService();
        } else if ("download".equals(request)) {
            return new FileDownloadService();
        } else if ("logout".equals(request)) {
            return new LogoutService();
        }
        return null;
    }
}

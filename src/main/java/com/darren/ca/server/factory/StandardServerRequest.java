package com.darren.ca.server.factory;

import com.darren.ca.server.service.*;

import static com.darren.ca.server.constants.RequestProtocol.*;

public class StandardServerRequest extends RequestFactory {
    @Override
    protected ClientRequest createClientRequest(short requestCode) {
        switch (requestCode) {
            case LOGIN:
                return new LoginService();
            case UPLOAD:
                return new FileUploadService();
            case DOWNLOAD:
                return new FileDownloadService();
            case LOGOUT:
                return new LogoutService();
            default:
                return null;
        }
    }
}

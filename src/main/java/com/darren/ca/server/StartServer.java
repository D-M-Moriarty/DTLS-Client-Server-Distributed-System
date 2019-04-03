package com.darren.ca.server;

import com.darren.ca.server.factory.RequestFactory;
import com.darren.ca.server.factory.StandardServerRequest;
import com.darren.ca.server.payload.DTLSDatagramSocket;
import com.darren.ca.server.payload.ServerSocketDatagram;

import java.net.SocketException;

import static com.darren.ca.server.constants.ServerProperties.SERVER_PORT;

class StartServer {
    private StartServer() {
    }

    public static void main(String[] args) throws SocketException {
        // Create Server
        Server server = Server.getServer();
//        Create request factory
        RequestFactory factory = new StandardServerRequest();
//        Create appropriate socket
//        default port used for server
        ServerSocketDatagram socketDatagram = new DTLSDatagramSocket(SERVER_PORT);
//        Start server
        server.start(factory, socketDatagram);
    }
}

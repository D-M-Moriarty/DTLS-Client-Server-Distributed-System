package com.darren.ca.server;

import com.darren.ca.server.factory.RequestFactory;
import com.darren.ca.server.model.DataPacket;
import com.darren.ca.server.payload.Response;
import com.darren.ca.server.payload.ServerSocketDatagram;
import com.darren.ca.server.service.ClientRequest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    // Singleton instance
    private static final Server server = new Server();
    //    factory for determining the correct services to handle the client requests
    private RequestFactory requestFactory;

    //    private constructor so stop duplicate instantiation in the JVM
    private Server() {
    }

    @Contract(pure = true)
    static Server getServer() {
        return server;
    }

    void start(RequestFactory requestFactory, ServerSocketDatagram mySocket) {
        this.requestFactory = requestFactory;
        connect(mySocket);
    }

    //   connect server to specified port to accept requests
    private void connect(ServerSocketDatagram mySocket) {
        try {
            logger.info("Server ready to handle requests");
            // wait for incoming client requests
            waitForRequests(mySocket);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    //  handles the incoming requests and responds to client
    private void waitForRequests(@NotNull ServerSocketDatagram mySocket) throws Exception {
        // infinite loop
        for (; ; ) {
//          extract DataPacket from socket
            DataPacket requestData = mySocket.receiveMessageAndSender();
            logger.info("Request received");
//          use DataPacket operation code to determine the service the client wants to use
            short operationCode = requestData.getOperationCode();
//          the appropriate service can be retrieved with the request factory with code
            ClientRequest clientRequest = requestFactory.getClientRequest(operationCode);
//          the request data is passed to the chosen service, and a response is returned
            Response response = clientRequest.handleRequest(requestData);
            logger.debug("message received: {}", requestData.getMessage());
//          the service response is then returned to the client
            mySocket.sendFile(requestData.getHost(), requestData.getPort(), response.getResponseBytes());
        }
    }

}

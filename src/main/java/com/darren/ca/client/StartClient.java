package com.darren.ca.client;

public class StartClient {

    private StartClient() {
    }


    public static void main(String[] args) {
//        System.setProperty(
//                "javax.net.ssl.trustStore",
//                CLIENT_TRUSTSTORE
//        );
//        System.setProperty(
//                "javax.net.ssl.trustStorePassword",
//                CLIENT_TRUSTSTORE_PASSWORD
//        );
        new FileTransferClient();
    }
}

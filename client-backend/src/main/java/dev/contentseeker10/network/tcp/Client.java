package dev.contentseeker10.network.tcp;

import java.net.Socket;

public class Client {

    private Socket socket;

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }


}

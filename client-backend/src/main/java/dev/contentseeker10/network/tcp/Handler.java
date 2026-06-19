package dev.contentseeker10.network.tcp;

import java.io.IOException;
import java.net.Socket;

public class Handler extends Thread {

    private final Socket clientSocket;

    public Handler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        start();
    }

    public void run() {

    }
}

package dev.contentseeker10.network.context;

import java.io.IOException;
import java.net.Socket;

public class TcpConnectionContext implements ConnectionContext {

    private final Socket socket;

    public TcpConnectionContext(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void sendResponse(byte[] data) throws IOException {
        socket.getOutputStream().write(data);
        socket.getOutputStream().flush();
    }

    public Socket getSocket() {
        return socket;
    }
}

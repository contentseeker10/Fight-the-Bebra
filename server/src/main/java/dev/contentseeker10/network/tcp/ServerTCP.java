package dev.contentseeker10.network.tcp;

import dev.contentseeker10.network.context.ConnectionContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerTCP implements Runnable {

    private final ServerSocket serverSocket;

    private final List<Socket> clientSockets = new ArrayList<>();

    public ServerTCP(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try (serverSocket) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Client " + clientSocket.getInetAddress() + " connected");
                synchronized (clientSockets) {
                    clientSockets.add(clientSocket);
                }
                new HandlerTCP(clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            serverSocket.close();
            synchronized (clientSockets) {
                for (Socket socket : clientSockets) {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                }
                clientSockets.clear();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error stopping server socket: "+ e.getMessage());
        }
    }
}

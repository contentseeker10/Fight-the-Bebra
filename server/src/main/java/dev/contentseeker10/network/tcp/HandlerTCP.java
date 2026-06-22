package dev.contentseeker10.network.tcp;

import dev.contentseeker10.model.User;
import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.network.context.TcpConnectionContext;
import dev.contentseeker10.server.ServerManager;
import dev.contentseeker10.services.LobbyService;
import dev.contentseeker10.services.SessionService;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class HandlerTCP extends Thread {

    private final Socket clientSocket;

    public HandlerTCP(Socket clientSocket) {
        this.clientSocket = clientSocket;
        start();
    }

    public void run() {
        try (Socket socket = clientSocket; InputStream in = socket.getInputStream()) {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] header = readExactBytes(in, 16);
                int payloadLength = ByteBuffer.wrap(header).getInt(10);
                byte[] body = readExactBytes(in, payloadLength + 2);
                byte[] fullMessage = new byte[header.length + body.length];
                System.arraycopy(header, 0, fullMessage, 0, header.length);
                System.arraycopy(body, 0, fullMessage, header.length, body.length);

                ConnectionContext context = new TcpConnectionContext(clientSocket);
                ServerManager.getInstance().getRawQueue().put(new RequestContext<>(fullMessage, context));
            }
        } catch (IOException e) {
            ConnectionContext context = new TcpConnectionContext(clientSocket);
            User user = SessionService.getInstance().getSessionUser(context);
            LobbyService.getInstance().forceLeaveLobby(user);
            SessionService.getInstance().endSession(context);
            System.out.println("[SERVER] Client disconnected: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private byte[] readExactBytes(InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int read = in.read(buffer, bytesRead, length - bytesRead);
            if (read == -1) {
                throw new IOException("Client closed connection.");
            }
            bytesRead += read;
        }
        return buffer;
    }

}

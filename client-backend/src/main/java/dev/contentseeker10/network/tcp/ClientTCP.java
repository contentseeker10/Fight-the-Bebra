package dev.contentseeker10.network.tcp;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.coders.Decoder;
import dev.contentseeker10.network.coders.Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientTCP {

    private Socket socket;

    private final String host;
    private final int port;

    public ClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Message sendRequest(Message request) {
        ensureConnection();

        byte[] requestBytes = Encoder.encode(request);

        try {
            socket.getOutputStream().write(requestBytes);
            socket.getOutputStream().flush();
        } catch (IOException e) {

            // TODO: Write status to game client via bridge, not just logging it

            System.err.println("[CLIENT] Error sending request. Attempting to reconnect...");
            closeSocket();
            ensureConnection();
            try {
                socket.getOutputStream().write(requestBytes);
                socket.getOutputStream().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            InputStream in = socket.getInputStream();

            byte[] header = readExactBytes(in, 16);
            int payloadLength = ByteBuffer.wrap(header).getInt(10);
            byte[] body = readExactBytes(in, payloadLength + 2);
            byte[] fullMessage = new byte[header.length + body.length];
            System.arraycopy(header, 0, fullMessage, 0, header.length);
            System.arraycopy(body, 0, fullMessage, header.length, body.length);

            return Decoder.decode(fullMessage);
        } catch (IOException e) {
            System.err.println("[CLIENT] Error reading response. Closing connection.");
            closeSocket();
            throw new RuntimeException(e);
        }
    }

    public void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException _) {}
            socket = null;
        }
    }

    private void ensureConnection() {

        // TODO: Write status to game client via bridge, not just logging it

        int delay = 1000;
        while (socket == null || socket.isClosed() || !socket.isConnected()) {
            try {
                System.out.println("[CLIENT] Attempting to connect to server...");
                socket = new Socket(host, port);
                System.out.println("[CLIENT] Successfully connected.");
            } catch (IOException e) {
                System.out.println("[CLIENT] Server is unavailable. Retry in " + (delay / 1000) + " seconds...");
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                delay = Math.min(delay * 2, 60000);
            }
        }
    }

    private byte[] readExactBytes(InputStream in, int length) throws IOException {
        byte[] buffer = new byte[length];
        int bytesRead = 0;
        while (bytesRead < length) {
            int read = in.read(buffer, bytesRead, length - bytesRead);
            if (read == -1) {
                throw new IOException("Server closed connection.");
            }
            bytesRead += read;
        }
        return buffer;
    }
}

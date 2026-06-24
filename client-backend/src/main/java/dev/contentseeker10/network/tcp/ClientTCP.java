package dev.contentseeker10.network.tcp;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.ResponseListener;
import dev.contentseeker10.network.coders.Decoder;
import dev.contentseeker10.network.coders.Encoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientTCP {

    private static final ClientTCP INSTANCE = new ClientTCP();
    private ClientTCP() {}
    public static ClientTCP getInstance() { return INSTANCE; }

    private static final String HOST = "192.168.0.105";
    private static final int PORT = 9090;

    private Socket socket;
    
    private volatile String currentStatus = "CONNECTION_LOST";
    private java.util.function.Consumer<String> statusListener;

    public void setStatusListener(java.util.function.Consumer<String> statusListener) {
        this.statusListener = statusListener;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    private void notifyStatus(String status) {
        this.currentStatus = status;
        if (statusListener != null) {
            statusListener.accept(status);
        }
    }

    public void listenResponses(ResponseListener listener) {
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                ensureConnection();
                try (InputStream in = socket.getInputStream()) {
                    while (!Thread.currentThread().isInterrupted() && socket != null && !socket.isClosed()) {
                        Message responseMessage = Decoder.decode(readResponse(in));
                        if (listener != null) {
                            listener.onResponse(responseMessage);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[CLIENT] Server Listener Error: " + e.getMessage());
                    notifyStatus("CONNECTION_LOST");
                    closeSocket();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "Server-Listener").start();
    }

    private byte[] readResponse(InputStream in) throws IOException {
        byte[] header = readExactBytes(in, 16);
        int payloadLength = ByteBuffer.wrap(header).getInt(10);
        byte[] body = readExactBytes(in, payloadLength + 2);
        byte[] fullMessage = new byte[header.length + body.length];
        System.arraycopy(header, 0, fullMessage, 0, header.length);
        System.arraycopy(body, 0, fullMessage, header.length, body.length);
        return fullMessage;
    }

    public void sendRequest(Message request) {
        ensureConnection();
        byte[] requestBytes = Encoder.encode(request);
        try {
            socket.getOutputStream().write(requestBytes);
            socket.getOutputStream().flush();
        } catch (IOException e) {
            System.err.println("[CLIENT] Error sending request. Attempting to reconnect...");
            notifyStatus("CONNECTION_LOST");
            closeSocket();
            ensureConnection();
            try {
                socket.getOutputStream().write(requestBytes);
                socket.getOutputStream().flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
        int delay = 1000;
        while (socket == null || socket.isClosed() || !socket.isConnected()) {
            try {
                System.out.println("[CLIENT] Attempting to connect to server...");
                socket = new Socket(HOST, PORT);
                System.out.println("[CLIENT] Successfully connected.");
                notifyStatus("CONNECTED");
            } catch (IOException e) {
                System.out.println("[CLIENT] Server is unavailable. Retry in " + (delay / 1000) + " seconds...");
                notifyStatus("CONNECTION_LOST");
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

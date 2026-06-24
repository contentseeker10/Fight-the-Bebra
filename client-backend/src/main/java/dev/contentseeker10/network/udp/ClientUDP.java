package dev.contentseeker10.network.udp;

import dev.contentseeker10.message.Message;
import dev.contentseeker10.network.ResponseListener;
import dev.contentseeker10.network.coders.Decoder;
import dev.contentseeker10.network.coders.Encoder;

import java.io.IOException;
import java.net.*;

public class ClientUDP {

    private static final ClientUDP INSTANCE = new ClientUDP();
    private ClientUDP() {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    public static ClientUDP getInstance() { return INSTANCE; }

    private final DatagramSocket socket;

    private final int serverPort = 9091;
    private final InetAddress serverAddress = new InetSocketAddress("192.168.0.105", serverPort).getAddress();

    private static final int TIMEOUT = 2000;
    private static final int MAX_RETRIES = 3;

    public void listenResponses(ResponseListener listener) {
        new Thread(() -> {
            byte[] buffer = new byte[65635];
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
                    Message responseMessage = Decoder.decode(data);
                    if (listener != null) {
                        listener.onResponse(responseMessage);
                    }
                } catch (IOException e) {
                    if (socket.isClosed()) {
                        break;
                    }
                    System.err.println("[CLIENT UDP] Error receiving response: " + e.getMessage());
                }
            }
        }, "Client-UDP-Listener").start();
    }

    public void sendRequest(Message request) throws IOException {
        byte[] requestBytes = Encoder.encode(request);
        for (int retry = 0; retry <= MAX_RETRIES; retry++) {
            socket.send(new DatagramPacket(requestBytes, requestBytes.length, serverAddress, serverPort));
        }
    }

}

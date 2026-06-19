package dev.contentseeker10.network.tcp.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.contentseeker10.dto.BridgeRequestDTO;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.network.tcp.ClientTCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class BridgeTCP implements Runnable {

    private final ServerSocket bridgeSocket;

    public BridgeTCP(ServerSocket bridgeSocket) {
        this.bridgeSocket = bridgeSocket;
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ClientTCP client = ClientTCP.getInstance();

    @Override
    public void run() {
        try (bridgeSocket) {
            while (true) {
                try (Socket gameSocket = bridgeSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()))) {

                    String json;
                    while ((json = reader.readLine()) != null) {
                        // TODO: Parse JSON via JsonNode of Jackson
                        // TODO: Build Message from data
                        // TODO: Send response back to Game Client
                    }
                } catch (IOException e) {
                    System.err.println("[BRIDGE] Game client disconnected: " + e.getMessage());
                    client.closeSocket();
                    break;
                }
            }
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

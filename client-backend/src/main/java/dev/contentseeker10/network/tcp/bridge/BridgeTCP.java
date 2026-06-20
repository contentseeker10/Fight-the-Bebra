package dev.contentseeker10.network.tcp.bridge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.contentseeker10.message.CommandType;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.network.tcp.ClientTCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
                     BufferedReader reader = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
                     OutputStream os = gameSocket.getOutputStream()) {

                    System.out.println("[BRIDGE] Game Client connected");

                    String json;
                    while ((json = reader.readLine()) != null) {
                        JsonNode root = mapper.readTree(json);
                        CommandType command = CommandType.valueOf(root.get("command").asText());

                        JsonNode request = root.get("request");
                        String data = request.toString();

                        Payload payload = new Payload(command.getCode(), 0, data);
                        Message message = new Message((byte) 0x13, (byte) 2, 0, payload);

                        Message response = client.sendRequest(message);
                        String responseJson = response.getPayload().getData();

                        os.write((responseJson + "\n").getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                } catch (IOException e) {
                    System.err.println("[BRIDGE] Game client disconnected: " + e.getMessage());
                    client.closeSocket();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package dev.contentseeker10.network.tcp.bridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.contentseeker10.dto.ResponseDTO;
import dev.contentseeker10.message.CommandType;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.network.tcp.ClientTCP;
import dev.contentseeker10.network.udp.ClientUDP;

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

    private static final ClientTCP tcpClient = ClientTCP.getInstance();
    private static final ClientUDP udpClient = ClientUDP.getInstance();

    private volatile OutputStream gameOutput;

    private void sendStatusToGame(String status) {
        OutputStream os = gameOutput;
        if (os != null) {
            try {
                String responseJson = mapper.writeValueAsString(new ResponseDTO("SERVER_STATUS", "{\"status\":\"" + status + "\"}")) + "\n";
                byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                synchronized (os) {
                    os.write(bytes);
                    os.flush();
                }
            } catch (IOException e) {
                System.err.println("[BRIDGE] Error sending status to Game Client: " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        tcpClient.setStatusListener(status -> {
            System.out.println("[BRIDGE] Server connection status changed: " + status);
            sendStatusToGame(status);
        });

        tcpClient.listenResponses(message -> {
            OutputStream os = gameOutput;
            if (os != null) {
                try {
                    byte[] responseBytes = buildResponse(message);
                    synchronized (os) {
                        os.write(responseBytes);
                        os.flush();
                    }
                } catch (IOException e) {
                    System.err.println("[BRIDGE] Error sending message to Game Client: " + e.getMessage());
                }
            }
        });

        udpClient.listenResponses(message -> {
            OutputStream os = gameOutput;
            if (os != null) {
                try {
                    byte[] responseBytes = buildResponse(message);
                    synchronized (os) {
                        os.write(responseBytes);
                        os.flush();
                    }
                } catch (IOException e) {
                    System.err.println("[BRIDGE] Error sending message to Game Client: " + e.getMessage());
                }
            }
        });

        try (bridgeSocket) {
            while (true) {
                try (Socket gameSocket = bridgeSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(gameSocket.getInputStream()));
                     OutputStream os = gameSocket.getOutputStream()) {

                    System.out.println("[BRIDGE] Game Client connected");
                    gameOutput = os;
                    
                    // Immediately send current server status
                    sendStatusToGame(tcpClient.getCurrentStatus());

                    String json;
                    while ((json = reader.readLine()) != null) {
                        System.out.println("[BRIDGE] Received from Godot: " + json);
                        Message message = buildRequest(json);
                        CommandType cmd = CommandType.fromCode(message.getPayload().getCmdType());
                        if (cmd == CommandType.HANDSHAKE || cmd == CommandType.GAME_INPUT || cmd == CommandType.GAME_STATE) {
                            System.out.println("[BRIDGE] Routing to UDP: " + cmd);
                            udpClient.sendRequest(message);
                        } else {
                            System.out.println("[BRIDGE] Routing to TCP: " + cmd);
                            tcpClient.sendRequest(message);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[BRIDGE] Game Client disconnected: " + e.getMessage());
                    gameOutput = null;
                    tcpClient.closeSocket();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] buildResponse(Message message) throws JsonProcessingException {
        String commandStr = getResponseCommand(message.getPayload().getCmdType());
        String responseStr = message.getPayload().getData();
        String responseJson = mapper.writeValueAsString(new ResponseDTO(commandStr, responseStr)) + "\n";
        return responseJson.getBytes(StandardCharsets.UTF_8);
    }

    private Message buildRequest(String json) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json);
        CommandType command = CommandType.valueOf(root.get("command").asText());

        JsonNode request = root.get("request");
        String data = request.toString();

        Payload payload = new Payload(command.getCode(), 0, data);

        return new Message((byte) 0x13, (byte) 2, 0, payload);
    }

    private String getResponseCommand(int command) {
        return switch (command) {
            case 0 -> "RESPONSE";

            case 1 -> "REGISTER";
            case 2 -> "LOGIN";

            case 3 -> "CREATE_LOBBY";
            case 4 -> "JOIN_LOBBY";
            case 5 -> "UPDATE_LOBBY";
            case 6 -> "LEAVE_LOBBY";

            case 7 -> "START_GAME";
            
            case 8 -> "HANDSHAKE";
            case 9 -> "GAME_INPUT";
            case 10 -> "GAME_STATE";

            default -> "UNKNOWN";
        };
    }

}

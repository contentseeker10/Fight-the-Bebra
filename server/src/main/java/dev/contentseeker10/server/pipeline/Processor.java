package dev.contentseeker10.server.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.contentseeker10.dto.UserDTO;
import dev.contentseeker10.dto.auth.AuthRequestDTO;
import dev.contentseeker10.dto.auth.AuthResponseDTO;
import dev.contentseeker10.dto.auth.RegisterRequestDTO;
import dev.contentseeker10.dto.auth.RegisterResponseDTO;
import dev.contentseeker10.dto.lobby.*;
import dev.contentseeker10.message.CommandType;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.model.User;
import dev.contentseeker10.model.type.UserType;
import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.dto.game.*;
import dev.contentseeker10.model.GameSession;
import dev.contentseeker10.services.AuthorizationService;
import dev.contentseeker10.services.GameService;
import dev.contentseeker10.services.LobbyService;
import dev.contentseeker10.services.SessionService;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private static final AuthorizationService authorizationService = AuthorizationService.getInstance();
    private static final SessionService sessionService = SessionService.getInstance();
    private static final LobbyService lobbyService = LobbyService.getInstance();
    private static final GameService gameService = GameService.getInstance();

    private static final ObjectMapper mapper = new ObjectMapper();

    private final BlockingQueue<RequestContext<Message>> inputQueue;
    private final BlockingQueue<RequestContext<Message>> outputQueue;

    public Processor(BlockingQueue<RequestContext<Message>> inputQueue, BlockingQueue<RequestContext<Message>> outputQueue) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
    }

    public void process(RequestContext<Message> requestContext) {
        Message message = requestContext.getBody();
        Payload payload = message.getPayload();

        CommandType commandType = CommandType.fromCode(payload.getCmdType());
        String data = payload.getData();

        ConnectionContext context = requestContext.getConnection();

        String response = switch (commandType) {
            case REGISTER -> processRegister(data);
            case LOGIN -> processLogin(data, context);

            case CREATE_LOBBY -> processCreateLobby(context);
            case JOIN_LOBBY -> processJoinLobby(data, context);
            case UPDATE_LOBBY -> processUpdateLobby();
            case LEAVE_LOBBY -> processLeaveLobby(data, context);

            case START_GAME -> processStartGame(data, context);
            case HANDSHAKE -> processUdpHandshake(data, context);
            case GAME_INPUT -> processGameInput(data, context);

            case SEND_MSG -> processSendMsg(data, context);
            case END_GAME -> processEndGame(data, context);

            default -> "{'response': 'ServerTCP Error'}";
        };

        Payload responsePayload = new Payload(commandType.getCode(), 0, response);
        Message responseMessage = new Message((byte) 0x13, (byte) 1, message.getMessageId(), responsePayload);

        try {
            outputQueue.put(new RequestContext<>(responseMessage, requestContext.getConnection()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String processStartGame(String data, ConnectionContext context) {
        try {
            StartGameRequestDTO request = mapper.readValue(data, StartGameRequestDTO.class);
            String lobbyCode = request.lobbyCode();
            User admin = sessionService.getSessionUser(context);
            
            if (admin == null || !admin.equals(lobbyService.getLobbyAdmin(lobbyCode))) {
                return mapper.writeValueAsString(new StartGameResponseDTO(false, "Unauthorized: only admin can start game", "", 0));
            }
            
            boolean success = lobbyService.startGame(lobbyCode);
            if (!success) {
                return mapper.writeValueAsString(new StartGameResponseDTO(false, "Failed to start game: lobby not ready", "", 0));
            }
            
            User guest = lobbyService.getLobbyGuest(lobbyCode);
            if (guest == null) {
                return mapper.writeValueAsString(new StartGameResponseDTO(false, "Failed to start game: lobby has no guest", "", 0));
            }
            
            GameSession session = gameService.createSession(lobbyCode, admin.getId(), guest.getId());
            
            // Send push to guest over TCP
            ConnectionContext guestContext = sessionService.getSession(guest);
            if (guestContext != null) {
                StartGameResponseDTO guestResponse = new StartGameResponseDTO(true, "", session.getGuestToken(), 9091);
                String guestResponseStr = mapper.writeValueAsString(guestResponse);
                sendSingleUpdate(CommandType.START_GAME, guestResponseStr, guestContext);
            }
            
            // Return response for admin
            StartGameResponseDTO adminResponse = new StartGameResponseDTO(true, "", session.getAdminToken(), 9091);
            return mapper.writeValueAsString(adminResponse);
            
        } catch (Exception e) {
            System.err.println("[SERVER] Error processing start game: " + e.getMessage());
            try {
                return mapper.writeValueAsString(new StartGameResponseDTO(false, "Internal server error: " + e.getMessage(), "", 0));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String processUdpHandshake(String data, ConnectionContext context) {
        if (!(context instanceof dev.contentseeker10.network.context.UdpConnectionContext udpContext)) {
            return "{\"success\":false,\"error\":\"Not a UDP connection\"}";
        }
        try {
            UdpHandshakeDTO handshake = mapper.readValue(data, UdpHandshakeDTO.class);
            boolean success = gameService.registerUdpAddress(handshake.userId(), handshake.token(), udpContext);
            if (success) {
                System.out.println("[SERVER UDP] Authorized UDP address for userId: " + handshake.userId());
                return "{\"success\":true}";
            } else {
                System.out.println("[SERVER UDP] Authorization failed for userId: " + handshake.userId());
                return "{\"success\":false,\"error\":\"Invalid token or session\"}";
            }
        } catch (Exception e) {
            System.err.println("[SERVER UDP] Error processing handshake: " + e.getMessage());
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String processGameInput(String data, ConnectionContext context) {
        if (!(context instanceof dev.contentseeker10.network.context.UdpConnectionContext udpContext)) {
            return "{\"success\":false,\"error\":\"Not a UDP connection\"}";
        }
        try {
            Integer senderId = gameService.getUserIdByUdpContext(udpContext);
            if (senderId == null) {
                System.err.println("[SERVER UDP] Game input received from unauthorized UDP address: " + udpContext.getClientAddress() + ":" + udpContext.getClientPort());
                return "{\"success\":false,\"error\":\"Unauthorized UDP address\"}";
            }

            PlayerInputDTO input = mapper.readValue(data, PlayerInputDTO.class);
            dev.contentseeker10.network.context.UdpConnectionContext teammateUdp = gameService.getTeammateUdpContext(senderId);
            
            if (teammateUdp != null) {
                PlayerStateDTO state = new PlayerStateDTO(senderId, input.x(), input.y(), input.hp(), input.isAttacking());
                String stateJson = mapper.writeValueAsString(state);
                sendSingleUpdate(CommandType.GAME_STATE, stateJson, teammateUdp);
            }

            return "{\"success\":true}";
        } catch (Exception e) {
            System.err.println("[SERVER UDP] Error processing game input: " + e.getMessage());
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String processSendMsg(String data, ConnectionContext context) {
        User sender = sessionService.getSessionUser(context);
        if (sender == null) {
            return "{\"success\":false,\"error\":\"Unauthorized\"}";
        }

        dev.contentseeker10.model.Lobby lobby = lobbyService.findUserLobby(sender);
        if (lobby == null) {
            return "{\"success\":false,\"error\":\"Lobby not found\"}";
        }

        try {
            String messageText = mapper.readTree(data).get("message").asText();
            
            // Broadcast message to everyone in the lobby
            String chatPayload = mapper.writeValueAsString(java.util.Map.of(
                "sender", sender.getUsername(),
                "message", messageText
            ));

            User admin = lobby.getAdmin();
            User guest = lobby.getGuest();

            if (admin != null) {
                ConnectionContext c = sessionService.getSession(admin);
                if (c != null) sendSingleUpdate(CommandType.CHAT_MSG, chatPayload, c);
            }
            if (guest != null) {
                ConnectionContext c = sessionService.getSession(guest);
                if (c != null) sendSingleUpdate(CommandType.CHAT_MSG, chatPayload, c);
            }

            return "{\"success\":true}";
        } catch (Exception e) {
            System.err.println("[SERVER] Error processing send message: " + e.getMessage());
            return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String processEndGame(String data, ConnectionContext context) {
        User user = sessionService.getSessionUser(context);
        if (user == null) {
            return "{\"success\":false,\"error\":\"Unauthorized\"}";
        }

        try {
            EndGameRequestDTO request = mapper.readValue(data, EndGameRequestDTO.class);
            String lobbyCode = request.lobbyCode();
            
            dev.contentseeker10.model.Lobby lobby = lobbyService.findUserLobby(user);
            if (lobby == null || !lobby.getCode().equals(lobbyCode)) {
                return mapper.writeValueAsString(new EndGameResponseDTO(false, "Lobby not found", 0, null, null));
            }
            
            if (!user.equals(lobby.getAdmin())) {
                return mapper.writeValueAsString(new EndGameResponseDTO(false, "Unauthorized: only admin can end game", 0, null, null));
            }

            synchronized (lobby) {
                lobby.setType(dev.contentseeker10.model.type.LobbyType.WAITING);
                int sessionScore = request.adminScore() + request.guestScore();
                lobby.setRecordScore(lobby.getRecordScore() + sessionScore);
            }

            User admin = lobby.getAdmin();
            User guest = lobby.getGuest();

            if (request.adminScore() > admin.getRecordScore()) {
                admin.setRecordScore(request.adminScore());
                authorizationService.updateRecordScore(admin.getId(), request.adminScore());
            }
            if (guest != null && request.guestScore() > guest.getRecordScore()) {
                guest.setRecordScore(request.guestScore());
                authorizationService.updateRecordScore(guest.getId(), request.guestScore());
            }

            gameService.cleanSession(admin.getId());

            UserDTO adminDTO = new UserDTO(admin);
            UserDTO guestDTO = guest != null ? new UserDTO(guest) : null;
            EndGameResponseDTO response = new EndGameResponseDTO(true, "", lobby.getRecordScore(), adminDTO, guestDTO);
            String responseStr = mapper.writeValueAsString(response);

            if (guest != null) {
                ConnectionContext guestContext = sessionService.getSession(guest);
                if (guestContext != null) {
                    sendSingleUpdate(CommandType.END_GAME, responseStr, guestContext);
                }
            }

            return responseStr;
        } catch (Exception e) {
            System.err.println("[SERVER] Error processing end game: " + e.getMessage());
            try {
                return mapper.writeValueAsString(new EndGameResponseDTO(false, "Internal server error: " + e.getMessage(), 0, null, null));
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void sendSingleUpdate(CommandType type, String data, ConnectionContext to) {
        Payload updatePayload = new Payload(type.getCode(), 0, data);
        Message updateMessage = new Message((byte) 0x13, (byte) 1, 0, updatePayload);
        try {
            outputQueue.put(new RequestContext<>(updateMessage, to));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String processCreateLobby(ConnectionContext context) {
        User user = sessionService.getSessionUser(context);
        CreateLobbyResponseDTO response = lobbyService.createLobby(user);
        System.out.println("[SERVER] Create Lobby Requested" + " by " + sessionService.getSessionUser(context) + ", success: " + response.success());
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String processJoinLobby(String data, ConnectionContext context) {
        JoinLobbyRequestDTO request;
        JoinLobbyResponseDTO response;
        String responseStr;

        User user = sessionService.getSessionUser(context);

        try {
            request = mapper.readValue(data, JoinLobbyRequestDTO.class);
        } catch (JsonProcessingException e) {
            try {
                responseStr = mapper.writeValueAsString(new JoinLobbyResponseDTO(false, "Bad Request", null, 0));
                return responseStr;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        response = lobbyService.joinLobby(request.lobbyCode(), user);

        System.out.println("[SERVER] Join Lobby Requested," + " by " + user + ", success: " + response.success());

        if (response.success()) {
            String updateData;
            try {
                dev.contentseeker10.model.Lobby lobby = lobbyService.findUserLobby(user);
                int recordScore = lobby != null ? lobby.getRecordScore() : 0;
                updateData = mapper.writeValueAsString(new UpdateLobbyResponseDTO(true, "", new UserDTO[]{new UserDTO(user)}, recordScore));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            ConnectionContext to = sessionService.getSession(lobbyService.getLobbyAdmin(request.lobbyCode()));
            sendSingleUpdate(CommandType.UPDATE_LOBBY, updateData, to);
        }

        try {
            responseStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return responseStr;
    }

    private String processUpdateLobby() {
        try {
            return mapper.writeValueAsString(new UpdateLobbyResponseDTO(false, "Server Not Ready", null, 0));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String processLeaveLobby(String data, ConnectionContext context) {
        LeaveLobbyRequestDTO request;
        LeaveLobbyResponseDTO response;
        String responseStr;

        User user = sessionService.getSessionUser(context);

        try {
            request = mapper.readValue(data, LeaveLobbyRequestDTO.class);
        } catch (JsonProcessingException e) {
            try {
                responseStr = mapper.writeValueAsString(new LeaveLobbyResponseDTO(false, "Bad Request"));
                return responseStr;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        User admin = lobbyService.getLobbyAdmin(request.lobbyCode());
        User guest = lobbyService.getLobbyGuest(request.lobbyCode());
        dev.contentseeker10.model.Lobby lobby = lobbyService.findUserLobby(user);
        int recordScore = lobby != null ? lobby.getRecordScore() : 0;

        response = lobbyService.leaveLobby(request.lobbyCode(), user);

        System.out.println("[SERVER] Leave Lobby Requested," + " by " + user + ", success: " + response.success());

        if (response.success()) {
            String updateData;
            try {
                updateData = mapper.writeValueAsString(new UpdateLobbyResponseDTO(true, "", new UserDTO[]{}, recordScore));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            User userTo = user.equals(admin) ? guest : admin;
            ConnectionContext to = sessionService.getSession(userTo);
            sendSingleUpdate(CommandType.UPDATE_LOBBY, updateData, to);
        }

        try {
            responseStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return responseStr;
    }

    private String processRegister(String data) {
        RegisterRequestDTO request;
        RegisterResponseDTO response;
        String responseStr;

        try {
            request = mapper.readValue(data, RegisterRequestDTO.class);
        } catch (JsonProcessingException e) {
            try {
                responseStr = mapper.writeValueAsString(new RegisterResponseDTO(false, "Bad Request"));
                return responseStr;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        response = authorizationService.register(request);

        try {
            responseStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return responseStr;
    }

    private String processLogin(String data, ConnectionContext context) {
        AuthRequestDTO request;
        AuthResponseDTO response;
        String responseStr;

        try {
            request = mapper.readValue(data, AuthRequestDTO.class);
        } catch (JsonProcessingException e) {
            try {
                responseStr = mapper.writeValueAsString(new AuthResponseDTO(false, "Bad Request", null));
                return responseStr;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        response = authorizationService.authorize(request);

        if (response.success()) {
            User user = new User(response.user());
            sessionService.newSession(user, context);
        }

        try {
            responseStr = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return responseStr;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RequestContext<Message> requestContext = inputQueue.take();
                process(requestContext);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

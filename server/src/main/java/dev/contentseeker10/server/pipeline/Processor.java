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
import dev.contentseeker10.network.context.ConnectionContext;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.services.AuthorizationService;
import dev.contentseeker10.services.LobbyService;
import dev.contentseeker10.services.SessionService;

import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private static final AuthorizationService authorizationService = AuthorizationService.getInstance();
    private static final SessionService sessionService = SessionService.getInstance();
    private static final LobbyService lobbyService = LobbyService.getInstance();

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
                responseStr = mapper.writeValueAsString(new JoinLobbyResponseDTO(false, "Bad Request", null));
                return responseStr;
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        }

        response = lobbyService.joinLobby(request.lobbyCode(), user);

        if (response.success()) {
            String updateData;
            try {
                updateData = mapper.writeValueAsString(new UpdateLobbyResponseDTO(true, "", new UserDTO[]{new UserDTO(user)}));
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
            return mapper.writeValueAsString(new UpdateLobbyResponseDTO(false, "Server Not Ready", null));
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

        response = lobbyService.leaveLobby(request.lobbyCode(), user);

        if (response.success()) {
            String updateData;
            try {
                updateData = mapper.writeValueAsString(new UpdateLobbyResponseDTO(true, "", new UserDTO[]{}));
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

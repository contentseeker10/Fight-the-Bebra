package dev.contentseeker10.server.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.contentseeker10.dto.AuthRequestDTO;
import dev.contentseeker10.dto.AuthResponseDTO;
import dev.contentseeker10.dto.RegisterRequestDTO;
import dev.contentseeker10.dto.RegisterResponseDTO;
import dev.contentseeker10.message.CommandType;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;
import dev.contentseeker10.network.context.RequestContext;
import dev.contentseeker10.services.AuthorizationService;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class Processor implements Runnable {

    private static final AuthorizationService authorizationService = AuthorizationService.getInstance();

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

        String response = switch (commandType) {
            case REGISTER -> processRegister(data);
            case LOGIN -> processLogin(data);
            default -> "{'response': 'Server Error'}";
        };

        Payload responsePayload = new Payload(CommandType.RESPONSE.getCode(), 0, response);
        Message responseMessage = new Message((byte) 0x13, (byte) 1, message.getMessageId(), responsePayload);

        try {
            outputQueue.put(new RequestContext<>(responseMessage, requestContext.getConnection()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    private String processLogin(String data) {
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

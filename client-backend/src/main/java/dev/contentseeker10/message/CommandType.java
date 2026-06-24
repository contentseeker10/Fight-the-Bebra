package dev.contentseeker10.message;

public enum CommandType {
    UNKNOWN(-1),
    RESPONSE(0),

    REGISTER(1),
    LOGIN(2),

    CREATE_LOBBY(3),
    JOIN_LOBBY(4),
    UPDATE_LOBBY(5),
    LEAVE_LOBBY(6),

    START_GAME(7),

    HANDSHAKE(8),
    GAME_INPUT(9),
    GAME_STATE(10),
    
    SEND_MSG(11),
    CHAT_MSG(12);


    private final int code;

    CommandType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CommandType fromCode(int code) {
        return switch (code) {
            case 0 -> RESPONSE;

            case 1 -> REGISTER;
            case 2 -> LOGIN;

            case 3 -> CREATE_LOBBY;
            case 4 -> JOIN_LOBBY;
            case 5 -> UPDATE_LOBBY;
            case 6 -> LEAVE_LOBBY;

            case 7 -> START_GAME;

            case 8 -> HANDSHAKE;
            case 9 -> GAME_INPUT;
            case 10 -> GAME_STATE;
            
            case 11 -> SEND_MSG;
            case 12 -> CHAT_MSG;

            default -> UNKNOWN;
        };
    }
}

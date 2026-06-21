package dev.contentseeker10.message;

public enum CommandType {
    UNKNOWN(-1),
    RESPONSE(0),

    REGISTER(1),
    LOGIN(2),

    CREATE_ROOM(3),
    JOIN_ROOM(4),
    UPDATE_ROOM(5),
    LEAVE_ROOM(6),

    START_GAME(7);


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

            case 3 -> CREATE_ROOM;
            case 4 -> JOIN_ROOM;
            case 5 -> UPDATE_ROOM;
            case 6 -> LEAVE_ROOM;

            case 7 -> START_GAME;

            default -> UNKNOWN;
        };
    }
}

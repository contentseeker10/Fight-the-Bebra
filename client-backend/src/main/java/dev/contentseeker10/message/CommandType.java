package dev.contentseeker10.message;

public enum CommandType {
    UNKNOWN(-1),
    RESPONSE(0);

    // TODO: Add different command types


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

            // ...

            default -> UNKNOWN;
        };
    }
}

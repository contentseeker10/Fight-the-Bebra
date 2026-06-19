package dev.contentseeker10.message;

public class Payload {

    private final int cmdType;
    private final int userId;

    private final String data;

    public Payload(int cmdType, int userId, String data) {
        this.cmdType = cmdType;
        this.userId = userId;
        this.data = data;
    }

    public int getCmdType() {
        return cmdType;
    }

    public int getUserId() {
        return userId;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "cmdType=" + cmdType +
                ", userId=" + userId +
                ", data=" + data +
                '}';
    }
}
package dev.contentseeker10.message;

import java.nio.charset.StandardCharsets;

public class Message {

    private final byte magic;
    private final byte source;
    private final long messageId;
    private final int length;
    private final Payload payload;

    public Message(byte magic, byte source, long messageId, Payload payload) {
        this.magic = magic;
        this.source = source;
        this.messageId = messageId;
        this.length = payload.getData().getBytes(StandardCharsets.UTF_8).length;
        this.payload = payload;
    }

    public byte getMagic() {
        return magic;
    }

    public byte getSource() {
        return source;
    }

    public long getMessageId() {
        return messageId;
    }

    public int getLength() {
        return length;
    }

    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "magic=" + magic +
                "source=" + source +
                ", packetId=" + messageId +
                ", length=" + length +
                ", payload=" + payload +
                '}';
    }
}
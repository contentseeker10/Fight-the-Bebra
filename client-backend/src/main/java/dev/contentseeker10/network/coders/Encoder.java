package dev.contentseeker10.network.coders;

import dev.contentseeker10.crypto.Crc16;
import dev.contentseeker10.crypto.CryptoService;
import dev.contentseeker10.message.Message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Encoder {

    public static final byte MAGIC = 0x13;

    public static byte[] encode(Message message) {
        byte[] payload = message.getPayload().getData().getBytes(StandardCharsets.UTF_8);
        ByteBuffer payloadBuffer = ByteBuffer.allocate(payload.length + 8);
        payloadBuffer.putInt(message.getPayload().getCmdType());
        payloadBuffer.putInt(message.getPayload().getUserId());
        payloadBuffer.put(payload);

        byte[] encryptedPayload = CryptoService.encrypt(payloadBuffer.array());
        short payloadCrc16 = Crc16.calculateSrc(encryptedPayload);

        ByteBuffer headerBuffer = ByteBuffer.allocate(1 + 1 + 8 + 4);
        headerBuffer.put(message.getMagic());
        headerBuffer.put(message.getSource());
        headerBuffer.putLong(message.getMessageId());
        headerBuffer.putInt(encryptedPayload.length);
        short headerCrc16 = Crc16.calculateSrc(headerBuffer.array());

        ByteBuffer result = ByteBuffer.allocate(calcSize(encryptedPayload.length));
        result.put(headerBuffer.array());
        result.putShort(headerCrc16);
        result.put(encryptedPayload);
        result.putShort(payloadCrc16);

        return result.array();
    }

    private static int calcSize(int payloadLength) {
        byte magicSize = 1;
        byte srcSize = 1;
        byte messageIdSize = 8;
        byte lengthSize = 4;
        byte headerCrc16Size = 2;
        byte messageCrc16Size = 2;

        return magicSize + srcSize + messageIdSize
                + lengthSize + headerCrc16Size + messageCrc16Size
                + payloadLength;
    }

}

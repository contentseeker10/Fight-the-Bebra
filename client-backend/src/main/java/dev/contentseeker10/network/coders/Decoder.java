package dev.contentseeker10.network.coders;

import dev.contentseeker10.crypto.Crc16;
import dev.contentseeker10.crypto.CryptoService;
import dev.contentseeker10.message.Message;
import dev.contentseeker10.message.Payload;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Decoder {

    public static final byte MAGIC = 0x13;

    public static Message decode(byte[] data) {
        ByteBuffer packetBuffer = ByteBuffer.wrap(data);

        byte magic = packetBuffer.get();
        checkMagic(magic);

        byte source = packetBuffer.get();
        long packetId = packetBuffer.getLong();
        int length = packetBuffer.getInt();

        byte[] headerData = new byte[14];
        System.arraycopy(data, 0, headerData, 0, 14);
        short headerCrc16 = packetBuffer.getShort();
        checkCrc(headerCrc16, headerData);

        byte[] messageData = new byte[length];
        packetBuffer.get(messageData);
        short messageCrc16 = packetBuffer.getShort();
        checkCrc(messageCrc16, messageData);

        byte[] decryptedMessage = CryptoService.decrypt(messageData);
        ByteBuffer messageBuffer = ByteBuffer.wrap(decryptedMessage);

        int cmdType = messageBuffer.getInt();
        int userId = messageBuffer.getInt();
        byte[] payloadBytes = new byte[decryptedMessage.length - 8];
        messageBuffer.get(payloadBytes);
        String payloadData = new String(payloadBytes, StandardCharsets.UTF_8);

        Payload payload = new Payload(cmdType, userId, payloadData);
        return new Message(magic, source, packetId, payload);
    }

    private static void checkMagic(byte magic) {
        if (magic != MAGIC)
            throw new RuntimeException("Wrong magic number. Unknown packet.");
    }

    private static void checkCrc(short crc, byte[] data) {
        if (crc != Crc16.calculateSrc(data))
            throw new RuntimeException("Wrong Crc16. Packet is corrupted.");
    }

}

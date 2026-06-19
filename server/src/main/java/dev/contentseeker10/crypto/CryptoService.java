package dev.contentseeker10.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class CryptoService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] KEY = "dXNy4fSZ6UEJaYrg".getBytes(StandardCharsets.UTF_8);
    private static final byte[] IV = "LEYDjkVO9srppUzd".getBytes(StandardCharsets.UTF_8);

    public static byte[] encrypt(byte[] rawData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(rawData);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error.", e);
        }
    }

    public static byte[] decrypt(byte[] encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error. Check the key or data integrity.", e);
        }
    }

}

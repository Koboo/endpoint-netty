package eu.koboo.endpoint.core.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class EncryptUtils {

    private static final String ALGORITHM = "AES";
    private static final String HASH = "SHA-256";
    private static final String CIPHER_128 = "AES/ECB/PKCS5PADDING";

    public static byte[] encrypt(byte[] bytes, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_128);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(bytes);
    }

    public static byte[] decrypt(byte[] bytes, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_128);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(bytes);
    }

    public static SecretKey getKeyFromPassword(String password) {
        try {
            byte[] hashed = hashSHA256(password.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hashed, ALGORITHM);
        } catch (Exception e) {
            throw new NullPointerException("SecretKey can't be generated by '" + password + "': " + e.getMessage());
        }
    }

    public static byte[] secretKeyToBytes(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    public static SecretKey bytesToSecretKey(byte[] secret) {
        return new SecretKeySpec(secret, ALGORITHM);
    }

    public static byte[] hashSHA256(byte[] input) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH);
        return messageDigest.digest(input);
    }
}
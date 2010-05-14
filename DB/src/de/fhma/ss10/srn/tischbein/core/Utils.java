package de.fhma.ss10.srn.tischbein.core;

import java.security.InvalidKeyException;
import java.security.MessageDigest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    private static MessageDigest md5 = null;
    private static Cipher rsa = null;
    private static Cipher aes = null;

    static {
        try {
            Utils.md5 = MessageDigest.getInstance("MD5");
            Utils.rsa = Cipher.getInstance("RSA");
            Utils.aes = Cipher.getInstance("AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String toMD5(final String text) {
        return Utils.toMD5(text.getBytes());
    }

    public static String toMD5(final byte[] text) {
        Utils.md5.reset();
        Utils.md5.update(text);
        byte[] res = Utils.md5.digest();

        return Utils.toHex(res);
    }

    public static String toHex(final byte[] res) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : res) {
            if ((b <= 15) && (b >= 0)) {
                hexString.append("0");
            }

            hexString.append(Integer.toHexString(b & 0xff));
        }

        return hexString.toString();
    }

    public static byte[] fromHex(final String hex) {
        byte[] res = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length() / 2; i++) {
            int t = Integer.decode("#" + hex.substring(i * 2, i * 2 + 2));

            if (t < 129) {
                res[i] = (byte) t;
            } else {
                res[i] = (byte) (t - 256);
            }
        }

        return res;
    }

    public static byte[] encrypt(final byte[] message, String secret) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        while (secret.length() < 16) {
            secret += secret;
        }

        secret = secret.substring(0, 16);

        Utils.aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret.getBytes(), "AES"));

        byte[] res = Utils.aes.doFinal(message);

        return res;
    }

    public static byte[] decrypt(final byte[] cipher, String secret) throws IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        while (secret.length() < 16) {
            secret += secret;
        }

        secret = secret.substring(0, 16);

        Utils.aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret.getBytes(), "AES"));

        byte[] res = Utils.aes.doFinal(cipher);

        return res;
    }

}

package de.fhma.ss10.srn.tischbein.core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;

/**
 * Werkzeugklasse. Enthält viele Methoden zum Ent- und Verschlüsseln von Daten und zum Erzeugen von MD5-Summen.
 * 
 * @author Smolli
 */
public final class Utils {
    /** Hält den Wert für 0xff. */
    private static final int NIBBLE_MAX_VALUE = 16;
    /** Hält das Maximum eines Unsinged Byte (255). */
    private static final int UNSIGNED_BYTE_MAX_VALUE = 0xff;
    /** Hält die Schlüssellänge für den RSA-Alogrithmus. */
    private static final int RSA_KEY_SIZE = 1024;

    /** Enthält den MD5-Konverter. */
    private static MessageDigest md5 = null;
    /** Enthält den RSA-Algorithmus. */
    private static Cipher rsa = null;

    static {
        try {
            Utils.md5 = MessageDigest.getInstance("MD5");
            Utils.rsa = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Erzeugt aus dem übergebenen String ein Byte-Array. Der String muss aus hexadezimalen paaren zu je zwei Ziffern
     * bestehen. Leer-, Satz- oder Sonderzeichen sind nicht erlaubt.
     * 
     * @param hex
     *            Der Hex-String.
     * @return Gibt den String als Byte-Array zurück.
     */
    public static byte[] fromHexString(final String hex) {
        byte[] res = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length() / 2; i++) {
            int t = Integer.decode("#" + hex.substring(i * 2, i * 2 + 2));

            if (t <= Byte.MAX_VALUE) {
                res[i] = (byte) t;
            } else {
                res[i] = (byte) (t - Utils.UNSIGNED_BYTE_MAX_VALUE - 1);
            }
        }

        return res;
    }

    /**
     * Erzeugt ein Public-Private-Schlüsselpaar für den RSA-Algorithmus.
     * 
     * @return Gibt das Schlüsselpaar als {@link java.security.KeyPair KeyPair} zurück.
     * @throws NoSuchAlgorithmException
     *             Wird geworfen, wenn der RSA-Algorithmus nicht zur Verfügung steht.
     * @see java.security.KeyPair
     * @see java.security.KeyPairGenerator
     * @see java.security.SecureRandom
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpgen = null;
        SecureRandom random = new SecureRandom();

        kpgen = KeyPairGenerator.getInstance("RSA");

        kpgen.initialize(Utils.RSA_KEY_SIZE, random);

        KeyPair generatedKeyPair = kpgen.generateKeyPair();

        return generatedKeyPair;
    }

    /**
     * Konvertiert ein Byte-Array in einen Hex-String.
     * 
     * @param res
     *            Das zu konvertierende Byte-Array.
     * @return Das Array als Hex-String.
     */
    public static String toHexString(final byte[] res) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : res) {
            if ((b < Utils.NIBBLE_MAX_VALUE) && (b >= 0)) {
                hexString.append("0");
            }

            hexString.append(Integer.toHexString(b & Utils.UNSIGNED_BYTE_MAX_VALUE));
        }

        return hexString.toString();
    }

    /**
     * Konvertiert ein Byte-Array in eine MD5-Summe.
     * 
     * @param text
     *            Das zu konvertierende Byte-Array.
     * @return Die MD5-Summe als Byte-Array.
     */
    public static byte[] toMD5(final byte[] text) {
        Utils.md5.reset();
        Utils.md5.update(text);

        return Utils.md5.digest();
    }

    /**
     * Konvertiert einen String in eine MD5-Summe.
     * 
     * @param text
     *            Der zu konvertierende String.
     * @return Die MD5-Summe als Byte-Array.
     */
    public static byte[] toMD5(final String text) {
        return Utils.toMD5(text.getBytes());
    }

    /**
     * Konvertiert einen String in eine MD5-Summe und gibt ihn als hexadezimalen String aus.
     * 
     * @param text
     *            Der zu konvertierende String.
     * @return Der Hexstring.
     */
    public static String toMD5Hex(final String text) {
        return Utils.toHexString(Utils.toMD5(text));
    }

}

package de.fhma.ss10.srn.tischbein.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Werkzeugklasse. Enthält viele Methoden zum Konvertieren von Daten und zum Erzeugen von MD5-Summen.
 * 
 * @author Smolli
 */
public final class Utils {
    private static final int TEXT_BLOCK_WIDTH = 64;
    /** Hält den Wert für 0xff. */
    private static final int NIBBLE_MAX_VALUE = 16;
    /** Hält das Maximum eines Unsinged Byte (255). */
    private static final int UNSIGNED_BYTE_MAX_VALUE = 0xff;
    /** Hält den globalen SecureRandom-Generator. */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Enthält den MD5-Konverter. */
    private static MessageDigest md5 = null;

    static {
        try {
            Utils.md5 = MessageDigest.getInstance("MD5");
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

    public static byte[] fromHexText(final String text) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            sb.append(line);
        }

        return Utils.fromHexString(sb.toString());
    }

    /**
     * Stellt einen globalen Zufallszahlengenerator zur verfügung.
     * 
     * @return Ein {@link SecureRandom} Zufallszahlengenerator.
     */
    public static SecureRandom getRandom() {
        return Utils.SECURE_RANDOM;
    }

    public static <U extends Key> U loadKey(final String string) throws UtilsException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(Utils.fromHexString(string)));

            return (U) ois.readObject();
        } catch (Exception e) {
            throw new UtilsException("Kann den Schlüssel nicht konvertieren!", e);
        }
    }

    public static String saveKey(final Key key) throws UtilsException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(key);

            return Utils.toHexString(bos.toByteArray());
        } catch (Exception e) {
            throw new UtilsException("Kann den Schlüssel nicht konvertieren!", e);
        }
    }

    /**
     * Konvertiert ein Byte-Array in einen Hex-String.
     * 
     * @param hex
     *            Das zu konvertierende Byte-Array.
     * @return Das Array als Hex-String.
     */
    public static String toHexString(final byte[] hex) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : hex) {
            if ((b < Utils.NIBBLE_MAX_VALUE) && (b >= 0)) {
                hexString.append("0");
            }

            hexString.append(Integer.toHexString(b & Utils.UNSIGNED_BYTE_MAX_VALUE));
        }

        return hexString.toString();
    }

    public static String toHexText(final byte[] hex) {
        String line = Utils.toHexString(hex);
        StringBuilder sb = new StringBuilder();

        while (line.length() > Utils.TEXT_BLOCK_WIDTH) {
            sb.append(line.substring(0, Utils.TEXT_BLOCK_WIDTH));
            sb.append("\n");

            line = line.substring(Utils.TEXT_BLOCK_WIDTH);
        }

        sb.append(line);

        return sb.toString();
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

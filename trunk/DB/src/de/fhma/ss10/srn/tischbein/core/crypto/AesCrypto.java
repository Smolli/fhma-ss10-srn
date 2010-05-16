package de.fhma.ss10.srn.tischbein.core.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.fhma.ss10.srn.tischbein.core.UtilsException;

/**
 * AES-Crypto-Klasse.
 * 
 * @author Smolli
 */
public class AesCrypto {

    /** Der Java-interne Name für den AES-Algorithmus. */
    public static final String AES_ALGO_NAME = "AES";
    /** Die Standard-Schlüssellänge für den AES-Algorithmus in Bytes. */
    public static final int AES_KEY_SIZE = 16;

    /** Enthält den AES-Algorithmus. */
    private static Cipher cipher = null;
    static {
        try {
            AesCrypto.cipher = Cipher.getInstance("AES_ALGO_NAME");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Entschlüsselt einen Geheimtext mit dem AES-Algorithmus und dem übergebenen Passwort.
     * 
     * @param cipherText
     *            Der verschlüsselte Geheimtext.
     * @param secret
     *            Der geheime Schlüssel.
     * @return Gibt den Klartext zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn der Geheimtext nicht entschlüsselt werden konnte.
     */
    public static byte[] decrypt(final byte[] cipherText, final byte[] secret) throws UtilsException {
        try {
            if (secret.length != AesCrypto.AES_KEY_SIZE) {
                throw new IllegalArgumentException("Schlüssel muss 128 Bit lang sein!");
            }

            AesCrypto.cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret, AesCrypto.AES_ALGO_NAME));

            byte[] res = AesCrypto.cipher.doFinal(cipherText);

            return res;
        } catch (Exception e) {
            throw new UtilsException("Konnte den Geheimtext nicht entschlüsseln!", e);
        }
    }

    /**
     * Verschlüsselt einen Klartext mit dem Übergebenen Schlüssel mit dem AES-Algorithmus.
     * 
     * @param plainText
     *            Der Klartext.
     * @param secret
     *            Der geheime Schlüssel.
     * @return Gibt den Geheimtext zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn der Klartext nicht verschlüsselt werden konnte.
     */
    public static byte[] encrypt(final byte[] plainText, final byte[] secret) throws UtilsException {
        try {
            if (secret.length != AesCrypto.AES_KEY_SIZE) {
                throw new IllegalArgumentException("Schlüssel muss 128 Bit lang sein!");
            }

            AesCrypto.cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, AesCrypto.AES_ALGO_NAME));

            byte[] res = AesCrypto.cipher.doFinal(plainText);

            return res;
        } catch (Exception e) {
            throw new UtilsException("Kann den Klartext nicht verschlüsseln!", e);
        }
    }

}

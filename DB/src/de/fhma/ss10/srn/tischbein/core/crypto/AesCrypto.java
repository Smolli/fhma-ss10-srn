package de.fhma.ss10.srn.tischbein.core.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

/**
 * AES-Crypto-Klasse.
 * 
 * @author Smolli
 */
public final class AesCrypto {

    /**
     * Kleine Helferklasse um Passwörter in einen AES-Schlüssel zu verwandeln.
     * 
     * @author Smolli
     */
    private static class AesSecretKey implements SecretKey {

        /** Serial UID. */
        private static final long serialVersionUID = -5533240395956948224L;
        /** Hält die Schlüsseldaten. */
        private final byte[] secret;

        /**
         * Ctor.
         * 
         * @param pass
         *            Das Passwort.
         */
        public AesSecretKey(final String pass) {
            this.secret = Utils.toMD5(pass);
        }

        @Override
        public String getAlgorithm() {
            return AesCrypto.AES_ALGO_NAME;
        }

        @Override
        public byte[] getEncoded() {
            return this.secret.clone();
        }

        @Override
        public String getFormat() {
            return "RAW";
        }

    }

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(AesCrypto.class);
    /** Der Java-interne Name für den AES-Algorithmus. */
    public static final String AES_ALGO_NAME = "AES";
    /** Die Standard-Schlüssellänge für den AES-Algorithmus in Bytes. */
    public static final int AES_KEY_SIZE = 16;
    /** Die Standard-Schlüssellänge für den AES-Algorithmus in Bits. */
    public static final int AES_KEY_SIZE_BITS = AesCrypto.AES_KEY_SIZE * 8;
    /** Enthält den AES-Algorithmus. */
    private static Cipher cipher = null;

    static {
        try {
            AesCrypto.cipher = Cipher.getInstance(AesCrypto.AES_ALGO_NAME);
        } catch (final Exception e) {
            AesCrypto.LOG.error("Kann den AES-Cipher nicht erstellen!", e);
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
    public static byte[] decrypt(final byte[] cipherText, final SecretKey secret) throws UtilsException {
        try {
            AesCrypto.cipher.init(Cipher.DECRYPT_MODE, secret);

            final byte[] res = AesCrypto.cipher.doFinal(cipherText);

            return res;
        } catch (final Exception e) {
            throw new UtilsException("Konnte den Geheimtext nicht entschlüsseln!", e);
        }
    }

    /**
     * Verschlüsselt einen Klartext mit dem Übergebenen Schlüssel mit dem AES-Algorithmus.
     * 
     * @param message
     *            Der Klartext.
     * @param secret
     *            Der geheime Schlüssel.
     * @return Gibt den Geheimtext zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn der Klartext nicht verschlüsselt werden konnte.
     */
    public static byte[] encrypt(final byte[] message, final SecretKey secret) throws UtilsException {
        try {
            AesCrypto.cipher.init(Cipher.ENCRYPT_MODE, secret);

            final byte[] res = AesCrypto.cipher.doFinal(message);

            return res;
        } catch (final Exception e) {
            throw new UtilsException("Kann den Klartext nicht verschlüsseln!", e);
        }
    }

    /**
     * Verschlüsselt einen Klartext mit dem Übergebenen Schlüssel mit dem AES-Algorithmus und gibt ihn als Hexstring
     * zurück.
     * 
     * @param message
     *            Der Klartext.
     * @param secret
     *            Der geheime Schlüssel.
     * @return Gibt den Geheimtext zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn der Klartext nicht verschlüsselt werden konnte.
     */
    public static String encryptHex(final byte[] message, final SecretKey secret) throws UtilsException {
        return Utils.toHexLine(AesCrypto.encrypt(message, secret));
    }

    /**
     * Generiert einen zufälligen AES-Schlüssel.
     * 
     * @return Der Schlüssel.
     * @throws CryptoException
     *             Wird geworfen, wenn der Schlüssel nicht generiert werden konnte.
     */
    public static SecretKey generateKey() throws CryptoException {
        try {
            final KeyGenerator generator = KeyGenerator.getInstance(AesCrypto.AES_ALGO_NAME);

            generator.init(AesCrypto.AES_KEY_SIZE_BITS, Utils.getRandom());

            return generator.generateKey();
        } catch (final Exception e) {
            throw new CryptoException("Kann den AES-Schlüssel nicht generieren!", e);
        }
    }

    /**
     * Erzeugt einen symmetrischen AES-Schlüssel anhand des übergebenen Passworts.
     * 
     * @param pass
     *            Das Passwort.
     * @return Gibt einen {@link SecretKey} zurück.
     * @throws CryptoException
     *             Wird geworfen, wenn der Schlüssel nicht erzeugt werden konnte.
     */
    public static SecretKey generateKey(final String pass) throws CryptoException {
        try {
            return new AesSecretKey(pass);
        } catch (final Exception e) {
            throw new CryptoException("Kann den AES-Schlüssel nicht generieren!", e);
        }
    }

    /**
     * Geschützter Ctor.
     */
    private AesCrypto() {
        super();
    }

}

package de.fhma.ss10.srn.tischbein.core.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

/**
 * Enthält lauter RSA-Methoden.
 * 
 * @author Smolli
 */
public class RsaCrypto {

    private static final String RSA_ALGO_NAME = "RSA";
    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bytes. */
    public static final int RSA_KEY_SIZE = 128;
    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bits. */
    public static final int RSA_KEY_SIZE_BITS = RsaCrypto.RSA_KEY_SIZE * 8;

    /** Enthält den RSA-Algorithmus. */
    private static Cipher cipher = null;

    static {
        try {
            RsaCrypto.cipher = Cipher.getInstance(RsaCrypto.RSA_ALGO_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] decode(final byte[] cipherText, final PrivateKey privateKey) throws UtilsException {
        try {
            RsaCrypto.cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] res = RsaCrypto.cipher.doFinal(cipherText);

            return res;
        } catch (Exception e) {
            throw new UtilsException("Kann den Text nicht entschlüsseln!", e);
        }
    }

    public static byte[] encode(final String string, final PublicKey publicKey) throws UtilsException {
        try {
            //            if (privateKey.length != RsaCrypto.RSA_KEY_SIZE) {
            //                throw new IllegalArgumentException("Schlüssel muss 128 Bit lang sein!");
            //            }

            RsaCrypto.cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] res = RsaCrypto.cipher.doFinal(string.getBytes());

            return res;
        } catch (Exception e) {
            throw new UtilsException("Kann den Klartext nicht verschlüsseln!", e);
        }
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
        kpgen = KeyPairGenerator.getInstance(RsaCrypto.RSA_ALGO_NAME);

        kpgen.initialize(RsaCrypto.RSA_KEY_SIZE_BITS, Utils.getRandom());

        KeyPair generatedKeyPair = kpgen.generateKeyPair();

        return generatedKeyPair;
    }

}

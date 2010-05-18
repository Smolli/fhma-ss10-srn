package de.fhma.ss10.srn.tischbein.core.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Enthält lauter RSA-Methoden.
 * 
 * @author Smolli
 */
public class RsaCrypto {

    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bytes. */
    public static final int RSA_KEY_SIZE = 128;
    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bits. */
    public static final int RSA_KEY_SIZE_BITS = RsaCrypto.RSA_KEY_SIZE * 8;

    /** Enthält den RSA-Algorithmus. */
    private static Cipher rsa = null;

    static {
        try {
            RsaCrypto.rsa = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
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
        kpgen = KeyPairGenerator.getInstance("RSA");

        kpgen.initialize(RsaCrypto.RSA_KEY_SIZE_BITS, Utils.getRandom());

        KeyPair generatedKeyPair = kpgen.generateKeyPair();

        return generatedKeyPair;
    }

}

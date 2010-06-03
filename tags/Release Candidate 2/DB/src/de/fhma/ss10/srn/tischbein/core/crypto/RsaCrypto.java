package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

/**
 * Enthält lauter RSA-Methoden.
 * 
 * @author Smolli
 */
public final class RsaCrypto {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(RsaCrypto.class);
    /** Hält den Standardnamen für die RSA-Verschlüsselung. */
    private static final String RSA_ALGO_NAME = "RSA";
    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bytes. */
    public static final int RSA_KEY_SIZE = 128;
    /** Hält die Größe des Padding-Blocks. */
    private static final int RSA_PADDING_SIZE = 11;
    /** Hält die maximale Blockgröße für die Verschlüsselung. */
    private static final int RSA_BLOCK_SIZE = RsaCrypto.RSA_KEY_SIZE - RsaCrypto.RSA_PADDING_SIZE;
    /** Hält die Schlüssellänge für den RSA-Alogrithmus in Bits. */
    public static final int RSA_KEY_SIZE_BITS = RsaCrypto.RSA_KEY_SIZE * 8;
    /** Enthält den RSA-Algorithmus. */
    private static Cipher cipher = null;

    static {
        try {
            RsaCrypto.cipher = Cipher.getInstance(RsaCrypto.RSA_ALGO_NAME);
        } catch (final Exception e) {
            RsaCrypto.LOG.error("Kann den RSA-Cipher nicht erzeugen!", e);
        }
    }

    /**
     * Entschlüsselt den angegebenen Geheimtext mit dem privaten Schlüssel.
     * <p>
     * Die RSA-Implementierung in Java erlaubt nur eine bestimmte Datenmenge, die entschlüsselt werden kann. Da die
     * Padding-Informationen auch mit abgespeichert werden, muss zu der Länge der verschlüsselten Rohdaten r die Größe
     * des Padding-Blocks p hinzugerechnet werden. Dataus ergibt sie eine Gesamtlänge C vom:
     * <p>
     * C = r + p
     * <p>
     * Da wir hier einen 1024Bit-Schlüssel verwenden, ist der verschlüsselte Block immer 128 Bytes lang. Abzüglich der
     * Padding-Informationen ergibt sich daraus eine Rohdatenblockgröße r von 117 Bytes.
     * <p>
     * Um auch größere Nachrichten entschlüsseln zu können, werden hier beliebig viele Blöcke aneinander gereiht und
     * entschlüsselt.
     * 
     * @param cipherText
     *            Der Geheimtext.
     * @param privateKey
     *            Der private Schlüssel.
     * @return Gibt den Klartext als Bytefolge zurück
     * @throws UtilsException
     *             Wird geworfen, wenn der Text nicht entschlüsselt werden kann.
     */
    public static byte[] decode(final byte[] cipherText, final PrivateKey privateKey) throws UtilsException {
        try {
            RsaCrypto.cipher.init(Cipher.DECRYPT_MODE, privateKey);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ByteArrayInputStream stream = new ByteArrayInputStream(cipherText);
            final byte[] res = new byte[RsaCrypto.RSA_KEY_SIZE];
            int size = cipherText.length;

            while (size > 0) {
                size -= stream.read(res);

                buffer.write(RsaCrypto.cipher.doFinal(res));
            }

            stream.close();

            return buffer.toByteArray();
        } catch (final Exception e) {
            throw new UtilsException("Kann den Text nicht entschlüsseln!", e);
        }
    }

    /**
     * Verschlüsselt den angegebenen Klartext mit dem öffentlichen Schlüssel.
     * <p>
     * Die RSA-Implementierung in Java nur eine begrenzte Blockgröße B erlaubt. Diese errechnet sich aus der
     * Schlüssellänge k und dem Paddingblock p, der abhängig vom gewählten Padding-Mechanismus gesetzt ist:
     * <p>
     * B = k - p
     * <p>
     * Hier ist k 128 Bytes und p 11 Bytes. Daraus ergibt sich eine effektive Blocklänge von 117 Bytes. Um aber auch
     * größere Datenmengen speichern zu können, müssen die übergebenen Daten in die Blockgröße zerlegt und einzeln
     * verschlüsselt werden.
     * 
     * @param messageText
     *            Der Klartext.
     * @param publicKey
     *            Der öffentlich Schlüssel.
     * @return Gibt den Geheimtext als Bytefolge zurück
     * @throws UtilsException
     *             Wird geworfen, wenn der Text nicht verschlüsselt werden kann.
     */
    public static byte[] encode(final String messageText, final PublicKey publicKey) throws UtilsException {
        try {
            RsaCrypto.cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ByteArrayInputStream stream = new ByteArrayInputStream(messageText.getBytes());
            int size = messageText.length();
            final byte[] res = new byte[RsaCrypto.RSA_BLOCK_SIZE];

            while (size > 0) {
                size -= stream.read(res);

                buffer.write(RsaCrypto.cipher.doFinal(res));
            }

            stream.close();

            return buffer.toByteArray();
        } catch (final Exception e) {
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
        final KeyPairGenerator kpgen = KeyPairGenerator.getInstance(RsaCrypto.RSA_ALGO_NAME);

        kpgen.initialize(RsaCrypto.RSA_KEY_SIZE_BITS, Utils.getRandom());

        final KeyPair generatedKeyPair = kpgen.generateKeyPair();

        return generatedKeyPair;
    }

    /**
     * Geschützer Standard-Ctor.
     */
    private RsaCrypto() {
        super();
    }

}

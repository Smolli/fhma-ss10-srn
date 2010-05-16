package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Ein spezialisierter {@link BufferedReader}, der AES-verschlüsselte Dateien lesen kann.
 * 
 * @author Smolli
 */
public final class AESReader extends BufferedReader {

    /**
     * Initialisiert den Reader mit einer verschlüsselten Datei.
     * 
     * @param filename
     *            Der Dateiname.
     * @param mySecret
     *            Der Schlüssel.
     * @return Gibt den Reader zurück, der auf die entschlüsselten Daten zeigt.
     * @throws CryptoException
     *             Wird geworfen, wenn die Datei nicht entschlüsselt werden konnte.
     */
    public static AESReader createReader(final String filename, final byte[] mySecret) throws CryptoException {
        // Schlüssel testen
        AESReader.testKey(mySecret);

        // Cipher initialisieren
        Cipher cipher = AESReader.initCipher(mySecret);

        // Rohdaten lesen
        byte[] buffer = AESReader.readData(filename);

        // entschlüsseln und in einen Reader wandeln
        return new AESReader(AESReader.decodeAndWrap(cipher, buffer));
    }

    /**
     * Entschlüsselt den übergebenen Puffer und wandelt ihn in ein {@link Reader}-Objekt zur weiteren Verarbeitung.
     * 
     * @param cipher
     *            Der Cipher, mit dem der Puffer entschlüsselt werden soll.
     * @param encoded
     *            Der Puffer mit den Rohdaten.
     * @return Der Reader, der auf die entschlüsselten Daten zeigt.
     * @throws CryptoException
     *             Wird geworfen, wenn der Puffer nicht entschlüsselt werden konnte.
     */
    private static Reader decodeAndWrap(final Cipher cipher, final byte[] encoded) throws CryptoException {
        try {
            // Rohdaten entschlüsseln und in ein Array speichern.
            byte[] decoded = cipher.doFinal(encoded);

            // ein Stream-Wrapper für das Array erzeugen
            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);

            // den Stream in einen Reader wandeln
            InputStreamReader isr = new InputStreamReader(bais);

            return isr;
        } catch (Exception e) {
            throw new CryptoException("Kann die angegebene Datei nicht entschlüsseln!", e);
        }
    }

    /**
     * Erstellt ein AES-Cipher-Objekt mit dem übergenenen Schlüssel.
     * 
     * @param mySecret
     *            Der Schlüssel, mit dem der Cipher initialisiert wird.
     * @return Gibt das {@link Cipher}-Objekt zurück.
     */
    private static Cipher initCipher(final byte[] mySecret) {
        Cipher cipher;

        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(mySecret, Utils.AES_ALGO_NAME));
        } catch (Exception e) {
            throw new RuntimeException("Kann den AESReader nicht erstellen!", e);
        }
        return cipher;
    }

    /**
     * Liest die Rohdaten aus der angegebenen Datei. Die Datei muss existieren.
     * 
     * @param filename
     *            Der Dateiname.
     * @return Gibt ein {@link Byte}-Array mit den Rohdaten zurück.
     * @throws CryptoException
     *             wird geworfen, wenn die Datei nicht gelesen werden konnte.
     */
    private static byte[] readData(final String filename) throws CryptoException {
        byte[] buffer;

        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            buffer = new byte[(int) file.length()];

            fis.read(buffer);
            fis.close();
        } catch (Exception e) {
            throw new CryptoException("Kann die angegebene Datei nicht öffnen!", e);
        }
        return buffer;
    }

    /**
     * Testet den übergebenen Schlüssel auf Konsistenz.
     * 
     * @param mySecret
     *            Der Schlüssel.
     */
    private static void testKey(final byte[] mySecret) {
        if (mySecret.length != Utils.AES_KEY_SIZE) {
            throw new IllegalArgumentException("Der Geheimschlüssel muss 128 Bit lang sein!");
        }
    }

    /**
     * Erstellt ein neues AESFileReader-Objekt.
     * 
     * @param reader
     *            Das {@link Reader}-Objekt.
     */
    private AESReader(final Reader reader) {
        super(reader);
    }

}

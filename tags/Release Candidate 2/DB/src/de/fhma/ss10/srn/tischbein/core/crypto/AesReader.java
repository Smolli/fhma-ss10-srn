package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.crypto.SecretKey;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Ein spezialisierter {@link BufferedReader}, der AES-verschlüsselte Dateien lesen kann.
 * 
 * @author Smolli
 */
public final class AesReader extends BufferedReader {

    /**
     * Initialisiert den Reader mit einer verschlüsselten Datei.
     * 
     * @param filename
     *            Der Dateiname.
     * @param secret
     *            Der Schlüssel.
     * @return Gibt den Reader zurück, der auf die entschlüsselten Daten zeigt.
     * @throws CryptoException
     *             Wird geworfen, wenn die Datei nicht entschlüsselt werden konnte.
     */
    public static AesReader createReader(final String filename, final SecretKey secret) throws CryptoException {
        // Rohdaten lesen
        final byte[] buffer = AesReader.readData(filename);

        // entschlüsseln und in einen Reader wandeln
        return new AesReader(AesReader.decodeAndWrap(buffer, secret));
    }

    /**
     * Entschlüsselt den übergebenen Puffer und wandelt ihn in ein {@link Reader}-Objekt zur weiteren Verarbeitung.
     * 
     * @param encoded
     *            Der Puffer mit den Rohdaten.
     * @param secret
     *            Der Schllüssel.
     * @return Der Reader, der auf die entschlüsselten Daten zeigt.
     * @throws CryptoException
     *             Wird geworfen, wenn der Puffer nicht entschlüsselt werden konnte.
     */
    private static Reader decodeAndWrap(final byte[] encoded, final SecretKey secret) throws CryptoException {
        try {
            byte[] decoded;

            if (encoded.length == 0) {
                // Sonderfall, verschlüsselte Daten sind leer.
                decoded = new byte[0];
            } else {
                // Rohdaten entschlüsseln und in ein Array speichern.
                decoded = AesCrypto.decrypt(encoded, secret);
            }

            // ein Stream-Wrapper für das Array erzeugen
            final ByteArrayInputStream bais = new ByteArrayInputStream(decoded);

            // den Stream in einen Reader wandeln
            final InputStreamReader isr = new InputStreamReader(bais);

            return isr;
        } catch (final Exception e) {
            throw new CryptoException("Kann die angegebene Datei nicht entschlüsseln!", e);
        }
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
            final File file = new File(filename);
            final FileInputStream fis = new FileInputStream(file);
            buffer = new byte[(int) file.length()];

            fis.read(buffer);
            fis.close();
        } catch (final Exception e) {
            throw new CryptoException("Kann die angegebene Datei nicht öffnen!", e);
        }

        return Utils.fromHexText(new String(buffer));
    }

    /**
     * Erstellt ein neues AESFileReader-Objekt.
     * 
     * @param reader
     *            Das {@link Reader}-Objekt.
     */
    private AesReader(final Reader reader) {
        super(reader);
    }

}

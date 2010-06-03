package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.db.dbms.AbstractDatabaseStructure;

/**
 * Spezialisierter {@link BufferedReader} um eine RSA-verschlüsselte Datei Zeilenweise zu lesen.
 * 
 * @author Smolli
 */
public final class RsaReader extends BufferedReader {

    /**
     * Erzeugt ein neues {@link RsaReader}-Objekt.
     * 
     * @param filename
     *            Der Dateiname der verschlüsselten Datei.
     * @param privateKey
     *            Der private Schlüssel.
     * @return Gibt einen Reader zurück.
     * @throws CryptoException
     *             Wird geworfen, wenn die Datei nicht geöffnet werden kann.
     */
    public static RsaReader createReader(final String filename, final PrivateKey privateKey) throws CryptoException {
        // Rohdaten lesen
        final List<ByteBuffer> encrypts = new ArrayList<ByteBuffer>();
        final List<String> raws = new ArrayList<String>();

        RsaReader.readData(filename, raws, encrypts);

        // entschlüsseln und in einen Reader wandeln
        return new RsaReader(RsaReader.decodeAndWrap(raws, encrypts, privateKey));
    }

    /**
     * Entschlüsselt die einzelnen Zeilen.
     * 
     * @param rows
     *            Die Liste der unverschlüsselten Zeilenelemente.
     * @param encrypts
     *            Die List der verschlüsselten Zeilenelemente.
     * @param privateKey
     *            Der private Schlüssel.
     * @return Gibt einen {@link Reader} auf die entschlüsselten Zeilen zurück.
     * @throws CryptoException
     *             Wird geworfen, wenn die Puffer nicht entschlüsselt werden konnten.
     */
    private static Reader decodeAndWrap(final List<String> rows, final List<ByteBuffer> encrypts,
            final PrivateKey privateKey) throws CryptoException {
        try {
            final StringBuilder decrypted = new StringBuilder();

            for (int i = 0; i < rows.size(); i++) {
                decrypted.append(rows.get(i));
                decrypted.append(AbstractDatabaseStructure.SEPARATOR);
                decrypted.append(new String(RsaCrypto.decode(encrypts.get(i).array(), privateKey))); // NOPMD
                decrypted.append("\n");
            }

            return new StringReader(decrypted.toString());
        } catch (final Exception e) {
            throw new CryptoException("Kann den Text nicht entschlüsseln!", e);
        }
    }

    /**
     * Ließt den Dateiinhalt und speichert jede einzelne verschlüsselte Zeile in einem separaten {@link ByteBuffer}.
     * 
     * @param filename
     *            Der Dateiname.
     * @param raws
     *            Die Liste, in die die unverschlüsselten Zeilenelemente gespeichert werden sollen.
     * @param encrypts
     *            Die Liste, in die die verschlüsselten Zeilenelemente gespeichert werden sollen.
     * @throws CryptoException
     *             Wird geworfen, wenn die Datei nicht gelesen werden kann.
     */
    private static void readData(final String filename, final List<String> raws, final List<ByteBuffer> encrypts)
            throws CryptoException {
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            String line;

            while ((line = reader.readLine()) != null) { // NOPMD by smolli on 30.05.10 20:19
                final String[] cols = line.split(AbstractDatabaseStructure.SEPARATOR);

                raws.add(cols[0]);
                encrypts.add(ByteBuffer.wrap(Utils.fromHexLine(cols[1])));
            }

            reader.close();
        } catch (final Exception e) {
            throw new CryptoException("Kann die Datei nicht lesen!", e);
        }
    }

    /**
     * Versteckter Ctor.
     * 
     * @param reader
     *            übernimmt ein {@link Reader}-Objekt.
     */
    private RsaReader(final Reader reader) {
        super(reader);
    }

}

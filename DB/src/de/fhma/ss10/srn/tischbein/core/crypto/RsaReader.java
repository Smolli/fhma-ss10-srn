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
        List<ByteBuffer> buffers = RsaReader.readData(filename);

        // entschlüsseln und in einen Reader wandeln
        return new RsaReader(RsaReader.decodeAndWrap(buffers, privateKey));
    }

    /**
     * Entschlüsselt die einzelnen Zeilen.
     * 
     * @param buffers
     *            Die Luste der Zeilenpuffer.
     * @param privateKey
     *            Der private Schlüssel.
     * @return Gibt einen {@link Reader} auf die entschlüsselten Zeilen zurück.
     * @throws CryptoException
     *             Wird geworfen, wenn die Puffer nicht entschlüsselt werden konnten.
     */
    private static Reader decodeAndWrap(final List<ByteBuffer> buffers, final PrivateKey privateKey)
            throws CryptoException {
        try {
            StringBuilder sb = new StringBuilder();

            for (ByteBuffer byteBuffer : buffers) {
                sb.append(new String(RsaCrypto.decode(byteBuffer.array(), privateKey)));
                sb.append("\n");
            }

            return new StringReader(sb.toString());
        } catch (Exception e) {
            throw new CryptoException("Kann den Text nicht entschlüsseln!", e);
        }
    }

    /**
     * Ließt den Dateiinhalt und speichert jede einzelne verschlüsselte Zeile in einem separaten {@link ByteBuffer}.
     * 
     * @param filename
     *            Der Dateiname.
     * @return Gibt die einzelnen Zeilen in Puffern zurück.
     * @throws CryptoException
     *             Wird geworfen, wenn die Datei nicht gelesen werden kann.
     */
    private static List<ByteBuffer> readData(final String filename) throws CryptoException {
        try {
            List<ByteBuffer> lines = new ArrayList<ByteBuffer>();
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(ByteBuffer.wrap(Utils.fromHexLine(line)));
            }

            reader.close();

            return lines;
        } catch (Exception e) {
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

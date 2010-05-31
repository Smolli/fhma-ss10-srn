package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.db.dbms.AbstractDatabaseStructure;

/**
 * Hilfsklasse zum Hinzufügen einer Zeile zu einer RSA-verschlüsselten Datei.
 * 
 * @author Smolli
 */
public final class RsaAppender {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(RsaAppender.class);

    /**
     * Fügt eine einzelne Zeile zu einer RSA-verschlüsselten Datei hinzu.
     * 
     * @param filename
     *            Die Datei.
     * @param publicKey
     *            Der öffentliche Schlüssel der Datei.
     * @param message
     *            Der Teil der Zeile, der verschlüsselt werden soll.
     * @param rawMessage
     *            Der Teil der Zeile, der nicht verschlüsselt werden soll.
     * @throws UtilsException
     *             Wird geworfen, wenn die Zeile nicht hinzugefügt werden kann.
     */
    public static void appendLine(final String filename, final PublicKey publicKey, final String message,
            final String rawMessage) throws UtilsException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(new File(filename), true));
            final byte[] encoded = RsaCrypto.encode(message, publicKey);

            writer.write(rawMessage);
            writer.write(AbstractDatabaseStructure.SEPARATOR);
            writer.write(Utils.toHexLine(encoded));
            writer.write("\n");
        } catch (final Exception e) {
            throw new UtilsException("Kann die Zeile nicht schreiben!", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    RsaAppender.LOG.error("Kann die Datei nicht schließen!", e);
                }
            }
        }
    }

    /**
     * Geschützter Ctor.
     */
    private RsaAppender() {
        super();
    }

}

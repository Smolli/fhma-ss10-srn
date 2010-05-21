package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

/**
 * Hilfsklasse zum Hinzufügen einer Zeile zu einer RSA-verschlüsselten Datei.
 * 
 * @author Smolli
 */
public class RSAAppender {

    /**
     * Fügt eine einzelne Zeile
     * 
     * @param filename
     * @param publicKey
     * @param line
     * @throws UtilsException
     */
    public static void appendLine(final String filename, final PublicKey publicKey, final String line)
            throws UtilsException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(new File(filename), true));
            byte[] encoded = RsaCrypto.encode(line, publicKey);

            writer.write(Utils.toHexString(encoded));
            writer.write("\n");
        } catch (Exception e) {
            throw new UtilsException("Kann die Zeile nicht schreiben!", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

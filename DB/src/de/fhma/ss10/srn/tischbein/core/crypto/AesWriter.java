package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.crypto.SecretKey;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Ein spezialisierter {@link BufferedWriter}, der AES-verschlüsselte Dateien lesen kann.
 * 
 * @author Smolli
 */
public final class AesWriter extends BufferedWriter {

    /**
     * Erstellt einen neuen {@link AesWriter}, der die Daten in die Datei mit dem Namen übergebenen Dateinamen und dem
     * angegebenen Schlüssel speichert.
     * 
     * @param filename
     *            Der Dateiname der Datei.
     * @param secret
     *            Der geheime Schlüssel zur Verschlüsselung.
     * @return Gibt ein neuen {@link AesWriter}-Objekt zurück.
     */
    public static AesWriter createWriter(final String filename, final SecretKey secret) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        final AesWriter writer = new AesWriter(new OutputStreamWriter(buffer));

        writer.buffer = buffer;
        writer.filename = filename;
        writer.secret = secret;

        return writer;
    }

    /** Hält den Puffer-Stream im Speicher. */
    private ByteArrayOutputStream buffer;
    /** Hält den Ausgabedateinamen. */
    private String filename;
    /** Hält den geheimen Schlüssel. */
    private SecretKey secret;

    /**
     * Versteckter, privater Standard-Ctor.
     * 
     * @param writer
     *            Der {@link Writer}, mit dem der {@link AesWriter} verbunden ist.
     */
    private AesWriter(final Writer writer) {
        super(writer);
    }

    @Override
    public void close() throws IOException {
        BufferedOutputStream bos = null;

        this.flush();

        try {
            bos = new BufferedOutputStream(new FileOutputStream(this.filename));

            if (this.buffer.size() > 0) {
                final byte[] encrypted = AesCrypto.encrypt(this.buffer.toByteArray(), this.secret);

                bos.write(Utils.toHexText(encrypted).getBytes());
            }

        } catch (final Exception e) {
            throw new IOException("Kann Datei nicht speichern!", e);
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    /**
     * Schreibt eine Zeile in den Puffer und fügt einen Zeilenvorschub am Ende hinzu.
     * 
     * @param line
     *            Die zu schreibende Zeile.
     * @throws IOException
     *             Wir geworfen, wenn die Zeile nicht angehängt werden konnte.
     */
    public void writeLine(final String line) throws IOException {
        this.write(line.toCharArray());
        this.write("\n");
    }
}

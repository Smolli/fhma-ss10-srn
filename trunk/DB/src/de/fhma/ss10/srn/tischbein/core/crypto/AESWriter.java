package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.fhma.ss10.srn.tischbein.core.Utils;

public final class AESWriter extends BufferedWriter {

    private ByteArrayOutputStream buffer;
    private String filename;
    private byte[] secret;

    public static AESWriter createWriter(String filename, byte[] secret) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        AESWriter writer = new AESWriter(new OutputStreamWriter(buffer));

        writer.buffer = buffer;
        writer.filename = filename;
        writer.secret = secret;

        return writer;
    }

    private AESWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void close() throws IOException {
        BufferedOutputStream bos = null;

        this.flush();

        try {
            bos = new BufferedOutputStream(new FileOutputStream(this.filename));

            if (this.buffer.size() > 0) {
                byte[] encrypted = AesCrypto.encrypt(buffer.toByteArray(), secret);

                bos.write(Utils.toHexString(encrypted).getBytes());
            }

        } catch (Exception e) {
            throw new IOException("Kann Datei nicht speichern!", e);
        } finally {
            if (bos != null)
                bos.close();
        }
    }

    public void writeLine(String line) throws IOException {
        this.write(line.toCharArray());
        this.write("\n");
    }
}

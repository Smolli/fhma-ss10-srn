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

public final class RSAReader extends BufferedReader {

    public static RSAReader createReader(final String filename, final PrivateKey privateKey) throws CryptoException {
        // Rohdaten lesen
        List<ByteBuffer> buffers = RSAReader.readData(filename);

        // entschlüsseln und in einen Reader wandeln
        return new RSAReader(RSAReader.decodeAndWrap(buffers, privateKey));
    }

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

    private RSAReader(final Reader reader) {
        super(reader);
    }

}

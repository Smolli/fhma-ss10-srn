package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.fhma.ss10.srn.tischbein.core.UtilsException;

public class RSAAppender {

    public static void appendLine(final String filename, final byte[] publicKey, final String line)
            throws UtilsException, IOException {
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(new File(filename), true));
        byte[] encoded = RsaCrypto.encode(line.getBytes(), publicKey);

        writer.write(encoded);
        writer.write("\n".getBytes());

        writer.close();
    }

}

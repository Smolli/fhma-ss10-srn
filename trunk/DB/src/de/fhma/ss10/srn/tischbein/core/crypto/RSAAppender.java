package de.fhma.ss10.srn.tischbein.core.crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

public class RSAAppender {

    public static void appendLine(final String filename, final PublicKey publicKey, final String line)
            throws UtilsException, IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename), true));
        byte[] encoded = RsaCrypto.encode(line, publicKey);

        writer.write(Utils.toHexString(encoded));
        writer.write("\n");

        writer.close();
    }

}

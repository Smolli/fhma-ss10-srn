package de.fhma.ss10.srn.tischbein.core.crypto.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AESReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AESWriter;

public class AesFileTest {

    @Test
    public void createFile() {
        AESWriter w = AESWriter.createWriter("test.file", Utils.toMD5("1234"));

        try {
            w.write("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f\n");

            w.close();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void readFile() {
        try {
            AESReader r = AESReader.createReader("test.file", Utils.toMD5("1234"));

            assertEquals("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f", r.readLine());

            r.close();
        } catch (Exception e) {
            fail();
        }
    }
}

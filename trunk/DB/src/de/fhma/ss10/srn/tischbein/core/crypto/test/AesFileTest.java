package de.fhma.ss10.srn.tischbein.core.crypto.test;

import org.junit.Assert;
import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AESReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AESWriter;

/**
 * {@link AESReader}- & AESWriter-Tests.
 * 
 * @author Smolli
 */
public final class AesFileTest {

    /**
     * Testet, ob der {@link AESReader} und {@link AESWriter} auch wirklich funktionieren.
     */
    @Test
    public void makeAndRead() {
        this.createFile();

        this.readFile();
    }

    /**
     * Testet {@link AESWriter}.
     */
    private void createFile() {
        AESWriter w = AESWriter.createWriter("test.file", Utils.toMD5("1234"));

        try {
            w.write("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f\n");

            w.close();
        } catch (Exception e) {
            Assert.fail();
        }
    }

    /**
     * Testet {@link AESReader}.
     */
    private void readFile() {
        try {
            AESReader r = AESReader.createReader("test.file", Utils.toMD5("1234"));

            Assert.assertEquals("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f", r.readLine());

            r.close();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}

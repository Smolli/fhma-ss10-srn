package de.fhma.ss10.srn.tischbein.core.crypto.test;

import org.junit.Assert;
import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.AesReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AesWriter;

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
        try {
            AesWriter w = AesWriter.createWriter("test.file", AesCrypto.generateKey("1234"));

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
            AesReader r = AesReader.createReader("test.file", AesCrypto.generateKey("1234"));

            Assert.assertEquals("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f", r.readLine());

            r.close();
        } catch (Exception e) {
            Assert.fail();
        }
    }
}

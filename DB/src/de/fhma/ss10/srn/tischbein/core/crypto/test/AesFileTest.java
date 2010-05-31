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
    public void makeAndRead() { // NOPMD by smolli on 30.05.10 20:26
        this.createFile();

        this.readFile();
    }

    /**
     * Testet {@link AESWriter}.
     */
    private void createFile() {
        try {
            final AesWriter writer = AesWriter.createWriter("test.file", AesCrypto.generateKey("1234"));

            writer.write("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f\n");

            writer.close();
        } catch (final Exception e) {
            Assert.fail();
        }
    }

    /**
     * Testet {@link AESReader}.
     */
    private void readFile() {
        try {
            final AesReader reader = AesReader.createReader("test.file", AesCrypto.generateKey("1234"));

            Assert.assertEquals("0;000102030405060708090a0b0c0d0e0f;000102030405060708090a0b0c0d0e0f", reader
                    .readLine());

            reader.close();
        } catch (final Exception e) {
            Assert.fail();
        }
    }
}

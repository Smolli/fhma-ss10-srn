package de.fhma.ss10.srn.tischbein.core;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class User {

    public static User create(final String name, final String pass) {
        User user = new User();
        KeyPairGenerator rsa = null;
        SecureRandom random = new SecureRandom();
        KeyPair pair;

        user.setName(name);
        user.setPass(pass);

        try {
            rsa = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        rsa.initialize(1024, random);

        user.setKeyPair(rsa.generateKeyPair());

        return user;
    }

    private KeyPair keyPair;
    private String name;
    private String passHash;
    private byte[] privateKeyEncrypted;
    private byte[] privateKeyDecrypted = null;

    private void setKeyPair(final KeyPair pair) {
        this.keyPair = pair;
    }

    private void setName(final String name) {
        this.name = name;
    }

    private void setPass(final String pass) {
        this.passHash = Utils.toMD5(pass);
    }

    public String getName() {
        return this.name;
    }

    public String getPassHash() {
        return this.passHash;
    }

    private void setPassHash(final String hash) {
        this.passHash = hash;
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    private void setKey(final byte[] publicKey, final byte[] privateKey) {
        this.privateKeyEncrypted = privateKey;

        this.keyPair = new KeyPair(new PublicKey() {

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return publicKey;
            }

            @Override
            public String getAlgorithm() {
                return "AES";
            }
        }, new PrivateKey() {

            @Override
            public String getAlgorithm() {
                return "AES";
            }

            @Override
            public byte[] getEncoded() {
                return User.this.privateKeyDecrypted;
            }

            @Override
            public String getFormat() {
                return null;
            }
        });
    }

    public static User read(final String line) {
        User user = new User();
        String[] cols = line.split(";");

        user.setName(cols[0]);
        user.setPassHash(cols[1]);
        // REM: Privater Schlüssel ist immernoch verschlüsselt!
        user.setKey(Utils.fromHex(cols[2]), Utils.fromHex(cols[3]));

        return user;
    }

    public boolean unlock(final String pass) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String hash = Utils.toMD5(pass);

        if (!hash.equals(this.passHash)) {
            return false;
        }

        this.privateKeyDecrypted = Utils.decrypt(this.privateKeyEncrypted, pass);

        return true;
    }

}

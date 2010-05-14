package de.fhma.ss10.srn.tischbein.core;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class User {

    private final class PrivateUserKey implements PrivateKey {
        private static final long serialVersionUID = 8016515506145694357L;

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
            // TODO: Klären, was hier zurück gegeben werden muss!
            return null;
        }
    }

    private final class PublicUserKey implements PublicKey {
        private static final long serialVersionUID = -4004473296448455411L;

        @Override
        public String getAlgorithm() {
            return "AES";
        }

        @Override
        public byte[] getEncoded() {
            return User.this.publicKey;
        }

        @Override
        public String getFormat() {
            // TODO: Klären, was hier zurück gegeben werden muss!
            return null;
        }
    }

    public static User create(final String name, final String pass) throws NoSuchAlgorithmException {
        User user = new User();

        user.setName(name);
        user.setPass(pass);

        KeyPair generatedKeyPair = Utils.generateRSAKeyPair();

        user.setKeyPair(generatedKeyPair);

        return user;
    }

    static User parse(final String line) {
        User user = new User();
        String[] cols = line.split(";");

        user.setName(cols[0]);
        user.setPassHash(cols[1]);
        // REM: Privater Schlüssel ist immernoch verschlüsselt!
        user.setKey(Utils.fromHexString(cols[2]), Utils.fromHexString(cols[3]));

        return user;
    }

    private KeyPair keyPair;
    private String username;
    private String passHash;
    private byte[] privateKeyEncrypted;
    private byte[] publicKey;

    private byte[] privateKeyDecrypted = null;

    public String getName() {
        return this.username;
    }

    public String getPassHash() {
        return this.passHash;
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public boolean unlock(final String pass) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String hash = Utils.toHexString(Utils.toMD5(pass));

        if (!hash.equals(this.passHash)) {
            return false;
        }

        this.privateKeyDecrypted = Utils.decrypt(this.privateKeyEncrypted, pass);

        return true;
    }

    /**
     * Setzt das RSA-Schlüsselpaar auf die beiden privaten und öffentlichen Werte.
     * 
     * @param publicKeyValue
     *            Der Öffentliche Schlüssel.
     * @param privateKeyValue
     *            Der private Schlüssel.
     */
    private void setKey(final byte[] publicKeyValue, final byte[] privateKeyValue) {
        this.privateKeyEncrypted = privateKeyValue;
        this.publicKey = publicKeyValue;

        this.keyPair = new KeyPair(new PublicUserKey(), new PrivateUserKey());
    }

    /**
     * Setzt das Schlüsselpaar auf direktem Weg.
     * 
     * @param pair
     *            Das Schlüsselpaar.
     */
    private void setKeyPair(final KeyPair pair) {
        this.keyPair = pair;
    }

    /**
     * Setzt den Benutzernamen.
     * 
     * @param name
     *            Der Benutzername.
     */
    private void setName(final String name) {
        this.username = name;
    }

    /**
     * Ermittelt den Hash zu dem übergebenen Passwort und speichert diesen. Das Benutzerpasswort wird niemals im
     * programm verzeichnet oder gespeichert.
     * 
     * @param pass
     *            Das Benutzerpasswort.
     */
    private void setPass(final String pass) {
        this.passHash = Utils.toHexString(Utils.toMD5(pass));
    }

    /**
     * Setzt den Hash des Benutzerpassworts direkt.
     * 
     * @param hash
     *            Der Hashwert des Benutzerpassworts als hexadezimal dargestellte MD5-Summe.
     */
    private void setPassHash(final String hash) {
        this.passHash = hash;
    }

}

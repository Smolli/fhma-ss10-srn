package de.fhma.ss10.srn.tischbein.core.db;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import de.fhma.ss10.srn.tischbein.core.Utils;

public final class User implements Serializable {

    private final class UserPrivateKey implements PrivateKey {

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

    private final class UserPublicKey implements PublicKey {

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

    /** serial UID. */
    private static final long serialVersionUID = 2490122214436444047L;

    /**
     * Erzeugt einen neuen User und ein neues Schlüsselpaar.
     * 
     * @param name
     *            Der Benutzername.
     * @param pass
     *            Das Benutzerpasswort.
     * @return Gibt das erzeugte Benutzer-Objekt zurück.
     * @throws UserException
     *             Wird geworfen, wenn der AES-Slgorithmus nicht zur Verfügung steht.
     */
    public static User create(final String name, final String pass) throws UserException {
        try {
            User user = new User();

            user.setName(name);
            user.setPass(pass);

            KeyPair generatedKeyPair = Utils.generateRSAKeyPair();

            user.setKeyPair(generatedKeyPair);

            return user;
        } catch (Exception e) {
            throw new UserException("Kann den Benutzer nicht erzeugen!", e);
        }
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
    private byte[] privateKeyDecrypted = null;
    private byte[] publicKey;

    public String getName() {
        return this.username;
    }

    public String getPassHash() {
        return this.passHash;
    }

    /**
     * Gibt den privaten Schlüssel zurück. Der Wert wird nur dann zurück gegeben, wenn der Benutzer sich zuvor mit einem
     * gültigen Passwort authentifiziert hat. Das herausgeben eines privaten Schlüssels Dritter ist nicht möglich.
     * 
     * @return Gibt Den private Schlüssel zurück, wenn der Benutzer authentifiziert ist, andernfalls <code>null</code>.
     */
    public byte[] getPrivateKey() {
        return this.keyPair.getPrivate().getEncoded();
    }

    public byte[] getPublicKey() {
        return this.keyPair.getPublic().getEncoded();
    }

    public boolean unlock(final String pass) throws UserException {
        try {
            String hash = Utils.toHexString(Utils.toMD5(pass));

            if (!hash.equals(this.passHash)) {
                return false;
            }

            this.privateKeyDecrypted = Utils.decrypt(this.privateKeyEncrypted, pass);

            return true;
        } catch (Exception e) {
            throw new UserException("Kann den Benutzer nicht authentifizieren!", e);
        }
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

        this.keyPair = new KeyPair(new UserPublicKey(), new UserPrivateKey());
    }

    /**
     * Setzt das Schlüsselpaar auf direktem Weg.
     * 
     * @param pair
     *            Das Schlüsselpaar.
     */
    private void setKeyPair(final KeyPair pair) {
        this.privateKeyDecrypted = pair.getPrivate().getEncoded();
        this.publicKey = pair.getPublic().getEncoded();

        this.keyPair = new KeyPair(new UserPublicKey(), new UserPrivateKey());
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

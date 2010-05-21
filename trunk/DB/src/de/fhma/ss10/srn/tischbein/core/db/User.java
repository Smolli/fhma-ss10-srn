package de.fhma.ss10.srn.tischbein.core.db;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.MessageFormat;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaCrypto;

/**
 * Userklasse. Enthält alle Methoden zur Benutzerverwaltung.
 * 
 * @author Smolli
 */
public final class User implements Serializable {

    /**
     * Innere Klasse zum Verwalten des privaten Schlüssels.
     * 
     * @author Smolli
     */
    private final class UserPrivateKey implements PrivateKey {

        /** Serial UID. */
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

    /**
     * Innere Klasse zum Verwalten des öffentlichen Schlüssels.
     * 
     * @author Smolli
     */
    private final class UserPublicKey implements PublicKey {

        /** Serial UID. */
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

    /** Benutzer-Tabelle Privater Schlüssel. */
    private static final int COLUMN_PRIVATE_KEY = 4;
    /** Benutzer-Tabelle Öffentlicher Schlüssel. */
    private static final int COLUMN_PUBLIC_KEY = 3;
    /** Benutzer-Tabelle CryptKey. */
    private static final int COLUMN_CRYPT_KEY = 2;
    /** Benutzer-Tabelle Passwort. */
    private static final int COLUMN_PW_HASH = 1;
    /** Benutzer-Tabelle Benutzername. */
    private static final int COLUMN_NAME = 0;

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
    static User create(final String name, final String pass) throws UserException {
        try {
            User user = new User();

            KeyPair generatedKeyPair = RsaCrypto.generateRSAKeyPair();
            byte[] cryptoKey = AesCrypto.generateKey();

            user.setName(name);
            user.setPass(pass);
            user.setKeyPair(generatedKeyPair);
            user.cryptKeyDecrypted = cryptoKey;

            return user;
        } catch (Exception e) {
            throw new UserException("Kann den Benutzer nicht erzeugen!", e);
        }
    }

    /**
     * Parst eine Zeile der Benutzertabelle und gibt den Inhalt als Benutzer-Objekt zurück.
     * 
     * @param line
     *            Die Zeile der Benutzertabelle.
     * @return Das gepartse Benutzer-Objekt.
     * @throws CryptoException
     *             Wird geworfen, wenn der CryptoKey nicht gesetzt werden kann.
     */
    static User parse(final String line) throws CryptoException {
        User user = new User();
        String[] cols = line.split(";");

        user.setName(cols[User.COLUMN_NAME]);
        user.setPassHash(cols[User.COLUMN_PW_HASH]);
        user.setCryptKey(Utils.fromHexString(cols[User.COLUMN_CRYPT_KEY]));
        // REM: Privater Schlüssel ist immernoch verschlüsselt!
        user.setKey(Utils.fromHexString(cols[User.COLUMN_PUBLIC_KEY]), Utils
                .fromHexString(cols[User.COLUMN_PRIVATE_KEY]));

        return user;
    }

    /** Hält das RSA-Schlüsselpaar. */
    private KeyPair keyPair;
    /** Hält den Benutzernamen. */
    private String username;
    /** Hält den Hash-Wert des Benutzerpassworts. */
    private String passHash;
    /** Hält den verschlüsselten privaten Schlüssel. */
    private byte[] privateKeyEncrypted;
    /** Hält den entschlüsselten privaten Schlüssel. */
    private byte[] privateKeyDecrypted = null;
    /** Hält den öffentlichen Schlüssel. */
    private byte[] publicKey;
    /** Hält den verschlüsselten CryptKey für die AES-Verschlüsselung. */
    private byte[] cryptKeyEncrypted;
    /** Hält den entschlüsselten CryptKey für die AES-Verschlüsselung. */
    private byte[] cryptKeyDecrypted;
    /** Hält die Dateidaten für den Benutzer. */
    private transient FileListObject flo;

    /**
     * Fügt zu einem Benutzer eine Datei hinzu. Die Datei wird verschlüsselt und alle Änderungen an der Datenbank werden
     * vorgenommen.
     * 
     * @param filename
     *            Der Dateiname mit vollständigem Pfad.
     * @return Gibt das hinzugefügte {@link FileItem} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn keine Änderungen an den Tabellen vorgenommen werden konnte.
     */
    public FileItem addFile(final String filename) throws DatabaseException {
        try {
            // Filekey erstellen
            byte[] secret = AesCrypto.generateKey();

            // Dateiinhalt verschlüsseln + speichern
            FileItem fi = DatabaseStructure.createEncryptedFile(filename, secret);

            Database.getInstance().addFile(this, fi);

            System.out.println("Datei " + filename + " hinzugefügt.");

            return fi;
        } catch (Exception e) {
            throw new DatabaseException("Kann Datei nicht hinzufügen!", e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }

        User other = (User) obj;

        return this.getName().equals(other.getName());
    }

    /**
     * Gibt das {@link FileListObject} zurück, dass dem User angehört.
     * 
     * @return Das {@link FileListObject} des Benutzers.
     */
    public FileListObject getFileListObject() {
        return this.flo;
    }

    /**
     * Gibt den Benutzernamen zurück.
     * 
     * @return Der Benutzername.
     */
    public String getName() {
        return this.username;
    }

    /**
     * Gibt den Hash-Wert des Benutzerpassworts zurück.
     * 
     * @return Der Hash-Wert des Passworts.
     */
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

    /**
     * Gibt den öffentlichen Schlüssel des Benutzers zurück. Dieser Schlüssel kann von jedem eingesehen werden.
     * 
     * @return Der öffentliche Schlüssel.
     */
    public byte[] getPublicKey() {
        return this.keyPair.getPublic().getEncoded();
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * Schließt den Benutzer ab und macht seinen privaten Schlüssel wieder unzugänglich.
     */
    public void lock() {
        this.privateKeyEncrypted = null;
        this.cryptKeyEncrypted = null;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Versucht den Benutzer zu authentifizieren. Nur wenn das Passwort mit dem Hash-Wert des Benutzers übereinstimmt,
     * wird der Benutzer freigeschaltet und sein privater Schlüssel entschlüsselt.
     * 
     * @param pass
     *            Das Passwort des Benutzers.
     * @return Gibt <code>true</code> zurück, wenn der Benutzer authentifiziert werden konnte, ansonsten
     *         <code>false</code>.
     * @throws UserException
     *             Wird geworfen, wenn der private Schlüssel nicht entschlüsselt werden konnte.
     */
    public boolean unlock(final String pass) throws UserException {
        try {
            byte[] secret = Utils.toMD5(pass);

            String hash = Utils.toHexString(secret);

            if (!hash.equals(this.passHash)) {
                return false;
            }

            this.privateKeyDecrypted = AesCrypto.decrypt(this.privateKeyEncrypted, secret);
            this.cryptKeyDecrypted = AesCrypto.decrypt(this.cryptKeyEncrypted, secret);

            this.flo = Database.getInstance().getFileList(this);

            return true;
        } catch (Exception e) {
            throw new UserException("Kann den Benutzer nicht authentifizieren!", e);
        }
    }

    /**
     * Compiliert das User-Objekt und gibt es als Zeichenkette zurück. Die einzelnen Felder sind duch den
     * {@link Database#SEPARATOR} getrennt.
     * 
     * @param pass
     *            Der Schlüssel mit dem der private Schlüssel des Benutzers verschlüsselt werden soll.
     * @return Gibt die Benutzerinformation als Zeichenkette zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn die Benutzerinformation nicht erstellt werden kann.
     */
    String compile(final String pass) throws UtilsException {
        byte[] secret = Utils.toMD5(pass);

        String pri = Utils.toHexString(AesCrypto.encrypt(this.getPrivateKey(), secret));
        String pub = Utils.toHexString(this.getPublicKey());
        String crypt = Utils.toHexString(AesCrypto.encrypt(this.getCryptKey(), secret));

        return MessageFormat.format("{1}{0}{2}{0}{3}{0}{4}{0}{5}\n", // Formatzeile
                DatabaseModel.SEPARATOR, // 0 - Separator
                this.getName(), // 1 - Benutzername
                this.getPassHash(), // 2 - Hashwert des Benutzerpassworts
                crypt, // 3 - CryptKey
                pub, // 4 - öffentlicher Schlüssel
                pri // 5 - private Schlüssel (verschlüsselt)
                );
    }

    /**
     * Gibt den CryptKey für die AES-Verschlüsselung zurück. Wenn der Benutzer nicht freigeschaltet, wird
     * <code>null</code> zurück gegeben.
     * 
     * @return Der CryptKey.
     */
    byte[] getCryptKey() {
        return this.cryptKeyDecrypted;
    }

    /**
     * Setzt den verschlüsselten CryptKey für die AES-Verschlüsselung.
     * 
     * @param key
     *            Der verschlüsselte CryptKey. Er kann nur mit dem richtigen Passwort wieder entschlüsselt werden.
     * @throws CryptoException
     *             Wird geworfen, wenn die Schlüssellänge nicht {@value AesCrypto#AES_KEY_SIZE} entspricht.
     */
    private void setCryptKey(final byte[] key) throws CryptoException {
        this.cryptKeyEncrypted = key;
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

package de.fhma.ss10.srn.tischbein.core.db;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.MessageFormat;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaCrypto;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseStructure;

/**
 * Userklasse. Enthält alle Methoden zur Benutzerverwaltung.
 * 
 * @author Smolli
 */
public final class User {

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

    /**
     * Parst eine Zeile der Benutzertabelle und gibt den Inhalt als Benutzer-Objekt zurück.
     * 
     * @param line
     *            Die Zeile der Benutzertabelle.
     * @return Das gepartse Benutzer-Objekt.
     * @throws CryptoException
     *             Wird geworfen, wenn der CryptoKey nicht gesetzt werden kann.
     * @throws UtilsException
     *             Wird geworfen, wenn das Schlüsselpaar nicht geladen werden konnte.
     */
    public static User parse(final String line) throws CryptoException, UtilsException {
        User user = new User();
        String[] cols = line.split(DatabaseStructure.SEPARATOR);

        user.setName(cols[User.COLUMN_NAME]);
        user.setPassHash(cols[User.COLUMN_PW_HASH]);
        user.setCryptKey(Utils.fromHexLine(cols[User.COLUMN_CRYPT_KEY]));
        user.setKeyPair(new KeyPair((PublicKey) Utils.loadKey(cols[User.COLUMN_PUBLIC_KEY]), (PrivateKey) Utils
                .loadKey(cols[User.COLUMN_PRIVATE_KEY])));

        return user;
    }

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

    /** Hält das RSA-Schlüsselpaar. */
    private KeyPair keyPair;
    /** Hält den Benutzernamen. */
    private String username;
    /** Hält den Hash-Wert des Benutzerpassworts. */
    private String passHash;
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
            FileItem fi = Utils.createEncryptedFile(this, filename, secret);

            Database.getInstance().addFileItem(fi);

            System.out.println("Datei " + filename + " hinzugefügt.");

            return fi;
        } catch (Exception e) {
            throw new DatabaseException("Kann Datei nicht hinzufügen!", e);
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
    public String compile(final String pass) throws UtilsException {
        byte[] secret = Utils.toMD5(pass);

        String pri = Utils.saveKey(this.keyPair.getPrivate());
        String pub = Utils.saveKey(this.keyPair.getPublic());
        String crypt = Utils.toHexLine(AesCrypto.encrypt(this.getCryptKey(), secret));

        return MessageFormat.format("{1}{0}{2}{0}{3}{0}{4}{0}{5}\n", // Formatzeile
                DatabaseStructure.SEPARATOR, // 0 - Separator
                this.getName(), // 1 - Benutzername
                this.getPassHash(), // 2 - Hashwert des Benutzerpassworts
                crypt, // 3 - CryptKey
                pub, // 4 - öffentlicher Schlüssel
                pri // 5 - private Schlüssel (verschlüsselt)
                );
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
     * Gibt den CryptKey für die AES-Verschlüsselung zurück. Wenn der Benutzer nicht freigeschaltet, wird
     * <code>null</code> zurück gegeben.
     * 
     * @return Der CryptKey.
     */
    public byte[] getCryptKey() {
        return this.cryptKeyDecrypted;
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
     * Gibt den privaten Schlüssel des Benutzers zurück.
     * 
     * @return Der {@link PrivateKey} des Benutzers.
     */
    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    /**
     * Gibt den öffentlichen Schlüssel des Benutzers zurück.
     * 
     * @return Der {@link PublicKey} des Benutzers.
     */
    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * Schließt den Benutzer ab und macht seinen privaten Schlüssel wieder unzugänglich.
     */
    public void lock() {
        //        this.privateKeyEncrypted = null;
        this.keyPair = null;
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

            String hash = Utils.toHexLine(secret);

            if (!hash.equals(this.passHash)) {
                return false;
            }

            //            this.privateKeyDecrypted = AesCrypto.decrypt(this.privateKeyEncrypted, secret);
            this.cryptKeyDecrypted = AesCrypto.decrypt(this.cryptKeyEncrypted, secret);

            this.flo = Database.getInstance().getFileListObject(this);

            return true;
        } catch (Exception e) {
            throw new UserException("Kann den Benutzer nicht authentifizieren!", e);
        }
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
        this.passHash = Utils.toHexLine(Utils.toMD5(pass));
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

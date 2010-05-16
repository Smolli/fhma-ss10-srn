package de.fhma.ss10.srn.tischbein.core.db;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.MessageFormat;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;

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
    private static final int COLUMN_PRIVATE_KEY = 3;
    /** Benutzer-Tabelle Öffentlicher Schlüssel. */
    private static final int COLUMN_PUBLIC_KEY = 2;
    /** Benutzer-Tabelle Passwort. */
    private static final int COLUMN_PASSWORD = 1;
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

    /**
     * Parst eine Zeile der Benutzertabelle und gibt den Inhalt als Benutzer-Objekt zurück.
     * 
     * @param line
     *            Die Zeile der Benutzertabelle.
     * @return Das gepartse Benutzer-Objekt.
     */
    static User parse(final String line) {
        User user = new User();
        String[] cols = line.split(";");

        user.setName(cols[User.COLUMN_NAME]);
        user.setPassHash(cols[User.COLUMN_PASSWORD]);
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

    /**
     * Schließt den Benutzer ab und macht seinen privaten Schlüssel wieder unzugänglich.
     */
    public void lock() {
        this.privateKeyEncrypted = null;
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
     * Compiliert das User-Objekt und gibt es als Zeichenkette zurück. Die einzelnen Felder sind duch den
     * {@link Database#SEPARATOR} getrennt.
     * 
     * @param secret
     *            Der Schlüssel mit dem der private Schlüssel des Benutzers verschlüsselt werden soll.
     * @return Gibt die Benutzerinformation als Zeichenkette zurück.
     * @throws UtilsException
     *             Wird geworfen, wenn die Benutzerinformation nicht erstellt werden kann.
     */
    String compile(final String secret) throws UtilsException {
        String pri = Utils.toHexString(Utils.encrypt(this.getPrivateKey(), secret));
        String pub = Utils.toHexString(this.getPublicKey());

        return MessageFormat.format("{1}{0}{2}{0}{3}{0}{4}\n", // Formatzeile
                this.getName(), // 1 - Benutzername
                this.getPassHash(), // 2 - Hashwert des Benutzerpassworts
                pub, // 3 - öffentlicher Schlüssel
                pri, // 4 - private Schlüssel (verschlüsselt)
                Database.SEPARATOR // 0 - Separator
                );
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

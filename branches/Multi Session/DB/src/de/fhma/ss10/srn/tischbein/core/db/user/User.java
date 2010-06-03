package de.fhma.ss10.srn.tischbein.core.db.user;

import java.security.KeyPair;
import java.security.PublicKey;
import java.text.MessageFormat;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaCrypto;
import de.fhma.ss10.srn.tischbein.core.db.dbms.AbstractDatabaseStructure;
import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseChangeListener;

/**
 * Userklasse. Enthält alle Methoden zur Benutzerverwaltung.
 * 
 * @author Smolli
 */
public final class User extends UserBase implements DatabaseChangeListener {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(User.class);
    /** Benutzer-Tabelle Privater Schlüssel. */
    private static final int COLUMN_PRIV_KEY = 4;
    /** Benutzer-Tabelle Öffentlicher Schlüssel. */
    private static final int COLUMN_PUB_KEY = 3;
    /** Benutzer-Tabelle CryptKey. */
    private static final int COLUMN_CRYPT_KEY = 2;
    /** Benutzer-Tabelle Passwort. */
    private static final int COLUMN_PW_HASH = 1;
    /** Benutzer-Tabelle Benutzername. */
    private static final int COLUMN_NAME = 0;

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
            final User user = new User();

            final KeyPair generatedKeyPair = RsaCrypto.generateRSAKeyPair();
            final SecretKey cryptoKey = AesCrypto.generateKey();

            user.setName(name);
            user.setPass(pass);
            user.setKeyPair(generatedKeyPair);
            user.setCryptKey(cryptoKey);

            Database.addChangeListener(user);

            return user;
        } catch (final Exception e) {
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
     * @throws UtilsException
     *             Wird geworfen, wenn das Schlüsselpaar nicht geladen werden konnte.
     */
    public static User parse(final String line) throws CryptoException, UtilsException {
        final User user = new User();
        final String[] cols = line.split(AbstractDatabaseStructure.SEPARATOR);

        user.setName(cols[User.COLUMN_NAME]);
        user.setPassHash(cols[User.COLUMN_PW_HASH]);
        user.setCryptKeyCipher(Utils.fromHexLine(cols[User.COLUMN_CRYPT_KEY]));
        user.setPublicKey((PublicKey) Utils.deserializeKeyHex(cols[User.COLUMN_PUB_KEY]));
        user.setPrivateKeyCipher(Utils.fromHexLine(cols[User.COLUMN_PRIV_KEY]));

        return user;
    }

    /** Hält die Dateidaten für den Benutzer. */
    private transient UserDescriptor descriptor;

    /**
     * Versucht den Benutzer anhand des übergebenen Passworts zu authentifizieren.
     * 
     * @param pass
     *            Das Passwort.
     * @return Gibt den geheimen Schlüssel zurück, mit dem der Benutzer freigeschalten werden kann.
     * @throws CryptoException
     *             Wird geworfen, wenn das Passwort nicht in einen Schlüssel gewandelt werden konnte.
     * @throws UserException
     *             Wird geworfen, wenn das Passwort falsch ist.
     */
    public SecretKey authenticate(final String pass) throws CryptoException, UserException {
        final SecretKey secret = AesCrypto.generateKey(pass);

        final String hash = Utils.toHexLine(secret.getEncoded());

        if (!hash.equals(this.getPassHash())) {
            throw new UserException("Das Passwort stimmt nicht!");
        }

        return secret;
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
        try {
            final SecretKey cryptKey = this.getCryptKey();
            final String pri = Utils.encryptSerializedKeyHex(this.getPrivateKey(), cryptKey);
            final String pub = Utils.serializeKeyHex(this.getPublicKey());
            final String crypt = Utils.encryptSerializedKeyHex(cryptKey, AesCrypto.generateKey(pass));

            return MessageFormat.format("{1}{0}{2}{0}{3}{0}{4}{0}{5}", // Formatzeile
                    AbstractDatabaseStructure.SEPARATOR, // 0 - Separator
                    this.getName(), // 1 - Benutzername
                    this.getPassHash(), // 2 - Hashwert des Benutzerpassworts
                    crypt, // 3 - CryptKey
                    pub, // 4 - öffentlicher Schlüssel
                    pri // 5 - private Schlüssel (verschlüsselt)
                    );
        } catch (final Exception e) {
            throw new UtilsException("Kann den Benutzer nicht kompilieren!", e);
        }
    }

    @Override
    public void databaseChanged() {
        if (this.isLocked()) {
            return;
        }

        try {
            this.descriptor = Database.getInstance().getUserDescriptor(this);
        } catch (final Exception e) {
            User.LOG.error("Kann den Benutzer nicht auffrischen!", e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result;

        if (obj instanceof User) {
            final User other = (User) obj;

            result = this.getName().equals(other.getName());
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Gibt das {@link UserDescriptor} zurück, dass dem User angehört.
     * 
     * @return Das {@link UserDescriptor} des Benutzers.
     */
    public UserDescriptor getDescriptor() {
        return this.descriptor;
    }

    //    /**
    //     * Schließt den Benutzer ab und macht seinen privaten Schlüssel wieder unzugänglich.
    //     */
    //    public void lock() {
    //        this.setLocked(true);
    //    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /**
     * Setzt den {@link UserDescriptor} auf das übergebene Objekt.
     * 
     * @param descriptorObject
     *            Der {@link UserDescriptor}.
     */
    public void setDescriptor(final UserDescriptor descriptorObject) {
        this.descriptor = descriptorObject;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    protected void finalize() throws Throwable {
        Database.removeChangeListener(this);

        super.finalize();
    }

}

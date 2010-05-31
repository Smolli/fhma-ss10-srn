package de.fhma.ss10.srn.tischbein.core.db;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesCrypto;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;

/**
 * Basisklasse für {@link User}.
 * 
 * @author Smolli
 */
public class UserBase {

    /** Hält den Hash-Wert des Benutzerpassworts. */
    private transient String passHash;
    /** Hält den privaten Schlüssel. */
    private transient PrivateKey privateKey;
    /** Hält den verschlüsselten privaten Schlüssel. */
    private transient byte[] privateKeyCipher;
    /** Hält den Schlüssel für die AES-Verschlüsselung. */
    private transient SecretKey cryptKey;
    /** Hält den verschlüsselten Schlüssel für die AES-Verschlüsselung. */
    private transient byte[] cryptKeyCipher;
    /** Hält das öffentlichen Schlüssel. */
    private PublicKey publicKey;
    /** Hält den Benutzernamen. */
    private transient String username;
    /** Gibt an, ob der Benutzer gesperrt ist oder nicht. */
    private transient boolean locked = true;

    /**
     * Gibt den CryptKey für die AES-Verschlüsselung zurück. Wenn der Benutzer nicht freigeschaltet, wird
     * <code>null</code> zurück gegeben.
     * 
     * @return Der CryptKey.
     */
    public SecretKey getCryptKey() {
        return this.cryptKey;
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
        PrivateKey result = null;

        if (!this.isLocked()) {
            result = this.privateKey;
        }

        return result;
    }

    /**
     * Gibt den öffentlichen Schlüssel des Benutzers zurück.
     * 
     * @return Der {@link PublicKey} des Benutzers.
     */
    public PublicKey getPublicKey() {
        PublicKey result = null;

        if (!this.isLocked()) {
            result = this.publicKey;
        }

        return result;
    }

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
    protected SecretKey authenticate(final String pass) throws CryptoException, UserException {
        final SecretKey secret = AesCrypto.generateKey(pass);

        final String hash = Utils.toHexLine(secret.getEncoded());

        if (!hash.equals(this.passHash)) {
            throw new UserException("Das Passwort stimmt nicht!");
        }

        return secret;
    }

    /**
     * Gibt an, ob der Benutzer gesperrt ist, oder nicht.
     * 
     * @return Gibt <code>true</code> zurück, wenn der Benutzer gesperrt ist, andernfalls <code>false</code>.
     */
    protected boolean isLocked() {
        return this.locked;
    }

    /**
     * Setzt den AES-Schlüssel.
     * 
     * @param key
     *            Der Schlüssel.
     */
    protected void setCryptKey(final SecretKey key) {
        this.cryptKey = key;
    }

    /**
     * Setzt den verschlüsselten CryptKey für die AES-Verschlüsselung.
     * 
     * @param keyCipher
     *            Der verschlüsselte CryptKey. Er kann nur mit dem richtigen Passwort wieder entschlüsselt werden.
     */
    protected void setCryptKeyCipher(final byte[] keyCipher) {
        this.cryptKeyCipher = keyCipher.clone();
    }

    /**
     * Setzt das Schlüsselpaar auf direktem Weg.
     * 
     * @param pair
     *            Das Schlüsselpaar.
     */
    protected void setKeyPair(final KeyPair pair) {
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    /**
     * Sperrt oder entsperrt den Benutzer.
     * 
     * @param value
     *            Wert.
     */
    protected void setLocked(final boolean value) {
        this.locked = value;
    }

    /**
     * Setzt den Benutzernamen.
     * 
     * @param name
     *            Der Benutzername.
     */
    protected void setName(final String name) {
        this.username = name;
    }

    /**
     * Ermittelt den Hash zu dem übergebenen Passwort und speichert diesen. Das Benutzerpasswort wird niemals im
     * programm verzeichnet oder gespeichert.
     * 
     * @param pass
     *            Das Benutzerpasswort.
     */
    protected void setPass(final String pass) {
        this.passHash = Utils.toHexLine(Utils.toMD5(pass));
    }

    /**
     * Setzt den Hash des Benutzerpassworts direkt.
     * 
     * @param hash
     *            Der Hashwert des Benutzerpassworts als hexadezimal dargestellte MD5-Summe.
     */
    protected void setPassHash(final String hash) {
        this.passHash = hash;
    }

    /**
     * Setzt den verschlüsselten privaten Schlüssel.
     * 
     * @param keyCipher
     *            Der Schlüssel.
     */
    protected void setPrivateKeyCipher(final byte[] keyCipher) {
        this.privateKeyCipher = keyCipher.clone();
    }

    /**
     * Setzt den öffentlichen Schlüssel.
     * 
     * @param key
     *            Der Schlüssel.
     */
    protected void setPublicKey(final PublicKey key) {
        this.publicKey = key;
    }

    /**
     * Aktualisiert die Schlüssel und entschlüsselt diese bei Bedarf.
     * 
     * @param secret
     *            Der Schlüssel, der den privaten AES-Schlüssel entschlüsselt.
     * @throws UtilsException
     *             Wird geworfen, wenn die Schlüssel nicht aktualisiert werden können.
     */
    protected void updateKeys(final SecretKey secret) throws UtilsException {
        if (this.cryptKey == null) {
            this.cryptKey = (SecretKey) Utils.deserializeKey(AesCrypto.decrypt(this.cryptKeyCipher, secret));
        }
        if (this.privateKey == null) {
            this.privateKey = (PrivateKey) Utils
                    .deserializeKey(AesCrypto.decrypt(this.privateKeyCipher, this.cryptKey));
        }
    }

}

package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.core.db.user.UserDescriptor;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database extends AbstractDatabaseModel {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(Database.class);
    /** Singleton-Instanz der Datenbank. */
    private static Database instance = null;
    /** Hält alle gemeldeten Listener. */
    private static final List<DatabaseChangeListener> LISTENERS = new ArrayList<DatabaseChangeListener>();
    /** Hält das Lock-Objeckt für die getInstance()-Methode. */
    private static final Object LOCK_OBJECT = new Object();

    /**
     * Fügt einen Change-Listener hinzu.
     * 
     * @param listener
     *            Der Listener vom Typ {@link DatabaseChangeListener}.
     */
    public static void addChangeListener(final DatabaseChangeListener listener) {
        DatabaseIO.LOCK.lock();

        try {
            Database.LOG.debug("Registriere Database-Listener: " + listener.toString());

            Database.LISTENERS.add(listener);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt die Instanz der Datenbank zurück. Wenn die Datenbank nicht gestartet werden kann, wird eine
     * <code>RuntimeException</code> mit dem Grund geworfen.
     * 
     * @return Gibt die Instanz der Datenbank zurück.
     */
    public static Database getInstance() {
        DatabaseIO.LOCK.lock();

        try {
            synchronized (Database.LOCK_OBJECT) {
                if (Database.instance == null) {
                    try {
                        Database.instance = new Database();

                        Database.instance.open();
                    } catch (final Exception e) {
                        throw new RuntimeException("FATAL: Kann keine Instanz der Datenbank erzeugen!", e); // NOPMD
                    }
                }
            }

            return Database.instance;
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Entfernt einen Listener.
     * 
     * @param listener
     *            Ein Listener vom Typ {@link DatabaseChangeListener}.
     */
    public static void removeChangeListener(final DatabaseChangeListener listener) {
        DatabaseIO.LOCK.lock();

        try {
            Database.LOG.debug("Entferne Database-Listener: " + listener.toString());

            Database.LISTENERS.remove(listener);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Feuert das Event, dass sich die Datenbank geändert hat.
     */
    private static void fireChangeEvent() {
        DatabaseIO.LOCK.lock();

        try {
            // Der Listerners-Vector *MUSS* hier kopiert werden, da es sein kann, dass er sich ändert (-> Exception!)
            final List<DatabaseChangeListener> listeners = new ArrayList<DatabaseChangeListener>(Database.LISTENERS);

            for (final DatabaseChangeListener listener : listeners) {
                listener.databaseChanged();
            }
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Privater ctor, um die Instanziierung außerhalb zu verhindern.
     */
    private Database() {
        super();
    }

    /**
     * Fügt ein {@link FileItem} zur Datenbank hinzu.
     * 
     * @param item
     *            Das {@link FileItem}, das hinzugefügt werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn das {@link FileItem} nicht hinzugefügt werden konnte.
     */
    public void addFileItem(final FileItem item) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            // in Dateitabelle des Benutzers Eintrag schreiben
            this.addFileToUserTable(item);

            // in globale Dateitablle Eintrag schreiben
            this.addFileToGlobalTable(item);

            Database.LOG.info("Datei " + item.toString() + " hinzugefügt.");
        } catch (final Exception e) {
            throw new DatabaseException("Die Datei kann nicht hinzugefügt werden!", e);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Erzeugt einen neuen Benutzer mit dem übergebenen Namen und Passwort. Es werden alle Schlüssel und die
     * Benutzereigenen Tabellen angelegt.
     * 
     * @param name
     *            Der Benutzername.
     * @param pass
     *            Das benutzerpasswort.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht angelegt werden konnte.
     */
    public void createUser(final String name, final String pass) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            if (this.getUserMap().containsKey(name.toLowerCase(Locale.GERMAN))) {
                throw new DatabaseException("Benutzer existiert schon!");
            }

            final User user = User.create(name, pass);

            this.addUser(user, pass);

            Database.LOG.info("Benutzer " + name + " angelegt.");
        } catch (final Exception e) {
            throw new DatabaseException("Fehler beim Anlegen des neuen Benutzers!", e);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Löscht das angegebene {@link FileItem} vollständig aus der Datenbank.
     * 
     * @param item
     *            Das FileItem, das gelöscht werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn das FileItem nicht gelöscht werden kann.
     */
    public void deleteFileItem(final FileItem item) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            // aus globaler Files-Tabelle löschen
            this.removeFileFromGlobalTable(item);

            // aus Files-Tabelle des Owner löschen
            this.removeFileFromOwnerTable(item);

            // aus den Access-Tabllen aller anderen User löschen
            // aus Lend-Tabelle des Owner löschen
            this.removeFileFromAccessTables(item);

            // DONE: physiaklisches Löschen der Datei
            this.removeFileFromDisk(item);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Entzieht dem angegebenen Benutzer das Zugriffsrecht für die angegebene Datei.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Berechtigung nicht entzogen werden kann.
     */
    public void denyAccess(final User user, final FileItem file) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            if ((file == null) || (user == null)) {
                return;
            }

            if (!file.getOwner().getDescriptor().getLendList().containsFile(file, user)) {
                throw new DatabaseException("Der Benutzer ist nicht im Besitz der Zugriffserlaubnis!");
            }

            this.denyAccessToUser(user, file);

            this.removeRemarkFromOwner(user, file);
        } catch (final Exception e) {
            throw new DatabaseException("Kann das Recht nicht speichern!", e);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Gibt eine Datei aus der Map zurück und prüft vorher, ob sie existiert.
     * 
     * @param fileId
     *            Die ID der Datei.
     * @return Gibt das {@link FileItem}-Objekt der Datei zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei mit der ID nicht im System ist.
     */
    public FileItem getFile(final int fileId) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            if (!this.getFileMap().containsKey(fileId)) {
                throw new DatabaseException("Datei ist nicht bekannt!");
            }

            return this.getFileMap().get(fileId);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt die nächte Datei ID zurück. Die IDs werden Datenbankweit vergeben und sind eindeutig.
     * 
     * @return Die ID als {@link Integer}.
     */
    public int getNextFileId() {
        DatabaseIO.LOCK.lock();

        try {
            return this.getLastFileId() + 1;
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt das {@link User}-Objekt mit dem angegebenen Benutzernamen zurück.
     * 
     * @param name
     *            Der Benutzername.
     * @return Gibt den Benutzer zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer dem System nicht bekannt ist.
     */
    public User getUser(final String name) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            final String username = name.toLowerCase(Locale.GERMAN);

            if (!this.getUserMap().containsKey(username)) {
                throw new DatabaseException("Benutzer ist nicht bekannt!");
            }

            return this.getUserMap().get(username);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt ein {@link UserDescriptor} zurück, das alle eigenen und fremden Dateien enthält, auf den der übergebene
     * Benutzer Zugriff hat.
     * 
     * @param user
     *            Der Benutzer.
     * @return Das {@link UserDescriptor} mit den Dateien.
     * @throws CryptoException
     *             Wird geworfen, wenn eine der Tabellen nicht entschlüsselt werden konnte.
     * @throws DatabaseException
     *             Wird geworfen, wenn eine der Tabellen nicht geladen werden konnte.
     */
    public UserDescriptor getUserDescriptor(final User user) throws CryptoException, DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            final UserDescriptor descriptor = new UserDescriptor();

            descriptor.setFilesTable(this.loadUserFilesTable(user));
            descriptor.setAccessTable(this.loadUserAccessTable(user));
            descriptor.setLendTable(this.loadUserLendTable(user));

            return descriptor;
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt alle {@link User} in der Datenbank zurück.
     * 
     * @return Alle {@link User} als {@link List}.
     */
    public List<User> getUsers() {
        DatabaseIO.LOCK.lock();

        try {
            return new ArrayList<User>(this.getUserMap().values());
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Gibt alle Benutzer der Datenbank zurück, außer dem angegebenen Benutzer.
     * 
     * @param without
     *            Der {@link User}, der nicht in der Liste auftauchen soll.
     * @return Alle anderen Benutzer als {@link List}.
     */
    public List<User> getUsers(final User without) {
        DatabaseIO.LOCK.lock();

        try {
            final List<User> users = this.getUsers();

            users.remove(without);

            return users;
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Erteilt dem übergebenen Benutzer Zugriffsrechte auf die übergebene Datei.
     * 
     * @param user
     *            Der {@link User}, für den die Zugriffsrechte erteilt werden.
     * @param file
     *            Das {@link FileItem}, für das die Zugriffsrechte erteilt werden.
     * @throws DatabaseException
     *             Wird geworfen, wenn das Zugriffsrecht nicht erteilt werden konnte.
     */
    public void grantAccess(final User user, final FileItem file) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            if ((file == null) || (user == null)) {
                return;
            }

            if (file.getOwner().getDescriptor().getLendList().containsFile(file, user)) {
                throw new DatabaseException("Die Datei wurde schon dem Benutzer zugewiesen!");
            }

            this.grantAccessToUser(user, file);

            this.addRemarkToOwner(user, file);
        } catch (final Exception e) {
            throw new DatabaseException("Kann Recht nicht speichern!", e);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Ermittelt ob ein Benutzer mit dem angegebenen Namen in der Datenbank existiert.
     * 
     * @param name
     *            Der Name des Benutzers.
     * @return Gibt <code>true</code> zurück, wenn der Benutzer existiert, andernfalls <code>false</code>.
     */
    public boolean hasUser(final String name) {
        DatabaseIO.LOCK.lock();

        try {
            return this.getUserMap().containsKey(name.toLowerCase(Locale.GERMAN));
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Prüft ob der angegebene Benutzer vorhanden und eingeloggt ist.
     * 
     * @param name
     *            Der Benutzername.
     * @return Gibt <code>true</code> zurück, wenn der Benutzer eingeloggt ist, anderfalls <code>false</code>.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht ermittelt werden konnte.
     */
    public boolean isUserLoggedIn(final String name) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            boolean result = false;

            if (Database.getInstance().hasUser(name)) {
                final User user = Database.getInstance().getUser(name);

                if (!user.isLocked()) {
                    result = true;
                }
            }

            return result;
        } catch (final Exception e) {
            throw new DatabaseException("Kann den Benutzer nicht ermitteln!", e);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Sperrt einen Benutzer wieder von der Datenbank aus.
     * 
     * @param user
     *            Der Benutzer.
     */
    public void lock(final User user) {
        DatabaseIO.LOCK.lock();

        try {
            if (user == null) {
                return;
            }

            user.setLocked(true);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

    /**
     * Versucht den Benutzer zu authentifizieren. Nur wenn das Passwort mit dem Hash-Wert des Benutzers übereinstimmt,
     * wird der Benutzer freigeschaltet und sein privater Schlüssel entschlüsselt.
     * 
     * @param user
     *            Der Benutzer.
     * @param pass
     *            Das Passwort des Benutzers.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht authentifiziert werden konnte.
     */
    public void unlock(final User user, final String pass) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            final SecretKey secret = user.authenticate(pass);

            user.setLocked(false);

            user.updateKeys(secret);

            user.setDescriptor(Database.getInstance().getUserDescriptor(user));

            Database.LOG.info("Benutzer " + user.getName() + " authentifiziert.");
        } catch (final Exception e) {
            throw new DatabaseException("Kann den Benutzer nicht authentifizieren!", e);
        } finally {
            DatabaseIO.LOCK.unlock();

            Database.fireChangeEvent();
        }
    }

}

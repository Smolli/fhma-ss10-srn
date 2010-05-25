package de.fhma.ss10.srn.tischbein.core.db;

import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseFiles;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseModel;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database extends DatabaseModel {

    /** Singleton-Instanz der Datenbank. */
    private static Database instance = null;

    /**
     * Gibt die Instanz der Datenbank zurück. Wenn die Datenbank nicht gestartet werden kann, wird eine
     * <code>RuntimeException</code> mit dem Grund geworfen.
     * 
     * @return Gibt die Instanz der Datenbank zurück.
     */
    public static synchronized Database getInstance() {
        DatabaseFiles.LOCK.lock();

        try {
            if (Database.instance == null) {
                try {
                    Database.instance = new Database();

                    Database.instance.open();
                } catch (Exception e) {
                    e.printStackTrace();

                    throw new RuntimeException("FATAL: Kann keine Instanz der Datenbank erzeugen!", e);
                }
            }

            return Database.instance;
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

    /**
     * Setzt die Datenbankinstanz auf <code>null</code>.
     */
    private static void killInstance() {
        DatabaseFiles.LOCK.lock();

        Database.instance = null;

        DatabaseFiles.LOCK.unlock();
    }

    /**
     * Privater ctor, um die Instanziierung außerhalb zu verhindern.
     */
    private Database() {
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
        try {
            // in Dateitabelle des Benutzers Eintrag schreiben
            this.addFileToUserTable(item);

            // in globale Dateitablle Eintrag schreiben
            this.addFileToGlobalTable(item);

            this.shutdown();
        } catch (Exception e) {
            throw new DatabaseException("Die Datei kann nicht hinzugefügt werden!", e);
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
        DatabaseFiles.LOCK.lock();

        try {
            if (this.getUserMap().containsKey(name.toLowerCase())) {
                throw new DatabaseException("Benutzer existiert schon!");
            }

            User user = User.create(name, pass);

            this.addUser(user, pass);

            System.out.println("Benutzer " + name + " angelegt.");
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Anlegen des neuen Benutzers!", e);
        } finally {
            DatabaseFiles.LOCK.unlock();
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
        // aus globaler Files-Tabelle löschen
        this.removeFileFromGlobalTable(item);

        // aus Files-Tabelle des Owner löschen
        this.removeFileFromOwnerTable(item);

        // aus den Access-Tabllen aller anderen User löschen
        // aus Lend-Tabelle des Owner löschen
        this.removeFileFromAccessTables(item);

        // TODO: physiaklisches Löschen der Datei
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
        if ((file == null) || (user == null)) {
            return;
        }

        if (!file.getOwner().getDescriptor().getLendList().containsFile(file, user)) {
            throw new DatabaseException("Der Benutzer ist nicht im Besitz der Zugriffserlaubnis!");
        }

        try {
            this.denyAccessToUser(user, file);

            this.removeRemarkFromOwner(user, file);
        } catch (Exception e) {
            throw new DatabaseException("Kann das Recht nicht speichern!", e);
        } finally {
            this.shutdown();
        }
    }

    /**
     * Gibt eine Datei aus der Map zurück und prüft vorher, ob sie existiert.
     * 
     * @param id
     *            Die ID der Datei.
     * @return Gibt das {@link FileItem}-Objekt der Datei zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei mit der ID nicht im System ist.
     */
    public FileItem getFile(final int id) throws DatabaseException {
        if (!this.getFileMap().containsKey(id)) {
            throw new DatabaseException("Datei ist nicht bekannt!");
        }

        return this.getFileMap().get(id);
    }

    /**
     * Gibt die nächte Datei ID zurück. Die IDs werden Datenbankweit vergeben und sind eindeutig.
     * 
     * @return Die ID als {@link Integer}.
     */
    public int getNextFileId() {
        return this.getLastFileId() + 1;
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
        if (!this.getUserMap().containsKey(name.toLowerCase())) {
            throw new DatabaseException("Benutzer ist nicht bekannt!");
        }

        return this.getUserMap().get(name.toLowerCase());
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
        DatabaseFiles.LOCK.lock();

        try {
            UserDescriptor descriptor = new UserDescriptor();

            descriptor.setFilesTable(this.loadUserFilesTable(user));
            descriptor.setAccessTable(this.loadUserAccessTable(user));
            descriptor.setLendTable(this.loadUserLendTable(user));

            return descriptor;
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

    /**
     * Gibt alle {@link User} in der Datenbank zurück.
     * 
     * @return Alle {@link User} als {@link Vector}.
     */
    public Vector<User> getUsers() {
        return new Vector<User>(this.getUserMap().values());
    }

    /**
     * Gibt alle Benutzer der Datenbank zurück, außer dem angegebenen Benutzer.
     * 
     * @param without
     *            Der {@link User}, der nicht in der Liste auftauchen soll.
     * @return Alle anderen Benutzer als {@link Vector}.
     */
    public Vector<User> getUsers(final User without) {
        Vector<User> users = this.getUsers();

        users.remove(without);

        return users;
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
        if ((file == null) || (user == null)) {
            return;
        }

        if (file.getOwner().getDescriptor().getLendList().containsFile(file, user)) {
            throw new DatabaseException("Die Datei wurde schon dem Benutzer zugewiesen!");
        }

        try {
            this.grantAccessToUser(user, file);

            this.addRemarkToOwner(user, file);
        } catch (Exception e) {
            throw new DatabaseException("Kann Recht nicht speichern!", e);
        } finally {
            this.shutdown();
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
        return this.getUserMap().containsKey(name.toLowerCase());
    }

    /**
     * Fährt die Datenbank runter und speichert noch ausstehende Daten ab.
     */
    public void shutdown() {
        DatabaseFiles.LOCK.lock();

        try {
            Database.killInstance();
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

}

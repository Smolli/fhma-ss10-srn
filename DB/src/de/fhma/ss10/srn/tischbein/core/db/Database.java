package de.fhma.ss10.srn.tischbein.core.db;

import java.io.IOException;
import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AESWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.RSAAppender;
import de.fhma.ss10.srn.tischbein.core.db.FileListObject.UserFilePair;

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
        DatabaseStructure.LOCK.lock();

        try {
            if (Database.instance == null) {
                try {
                    Database.instance = Database.open();
                } catch (Exception e) {
                    e.printStackTrace();

                    throw new RuntimeException("FATAL: Kann keine Instanz der Datenbank erzeugen!", e);
                }
            }

            return Database.instance;
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    /**
     * Setzt die Datenbankinstanz auf <code>null</code>.
     */
    private static void killInstance() {
        Database.instance = null;
    }

    /**
     * Öffnet ein bestehendes Datenbankschema.
     * 
     * @return Gibt die geladene Datenbank zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datenbankstruktur korrupt ist oder die Datenbank aus anderen Gründen nicht
     *             geladen werden konnte.
     */
    private static Database open() throws DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            Database db = new Database();

            if (new java.io.File(DatabaseStructure.DB_USERS_TB).createNewFile()) {
                System.out.println("User-Tabelle angelegt.");
            }

            if (new java.io.File(DatabaseStructure.DB_FILES_TB).createNewFile()) {
                System.out.println("Dateien-Tabelle anegelegt.");
            }

            db.loadUsers();
            db.loadFiles();

            return db;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    /**
     * Privater ctor, um die Instanziierung außerhalb zu verhindern.
     */
    private Database() {
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
        DatabaseStructure.LOCK.lock();

        try {
            if (this.getUserMap().containsKey(name.toLowerCase())) {
                throw new DatabaseException("Benutzer existiert schon!");
            }

            User user = User.create(name, pass);

            this.createUserFiles(user);

            this.saveToUsers(user, pass);
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Anlegen des neuen Benutzers!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
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

        try {
            this.appendToUser(user, file);

            this.remarkToOwner(user, file);
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
        DatabaseStructure.LOCK.lock();

        try {
            Database.killInstance();
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    /**
     * Fügt ein {@link FileItem} zur Datenbank hinzu.
     * 
     * @param item
     *            Das {@link FileItem}, das hinzugefügt werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn das {@link FileItem} nicht hinzugefügt werden konnte.
     */
    void addFileItem(final FileItem item) throws DatabaseException {
        try {
            // in Dateitabelle des Benutzers Eintrag schreiben
            DatabaseModel.updateUserTables(item);

            // in globale Dateitablle Eintrag schreiben
            DatabaseModel.updateGlobalTable(item);

            this.shutdown();
        } catch (Exception e) {
            throw new DatabaseException("Die Datei kann nicht hinzugefügt werden!", e);
        }
    }

    /**
     * Fügt das Nutzungsrecht in der Access-Tabelle des {@link User} hinzu.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @throws UtilsException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
     */
    private void appendToUser(final User user, final FileItem file) throws UtilsException {
        RSAAppender.appendLine(Tables.AccessTable.getFilename(user), user.getPublicKey(), file.getId()
                + DatabaseModel.SEPARATOR + Utils.toHexString(file.getKey()));
    }

    /**
     * Merkt sich beim Besitzer der Datei, wohin er die Datei ausgeliehen hat.
     * 
     * @param user
     *            Der Besitzer der Datei.
     * @param file
     *            Die Datei.
     * @throws IOException
     *             Wird geworfen, wenn die Tabelle nicht gespeichert werden konnte.
     */
    private void remarkToOwner(final User user, final FileItem file) throws IOException {
        User owner = file.getOwner();
        AESWriter w = AESWriter.createWriter(Tables.LendTable.getFilename(owner), owner.getCryptKey());

        Vector<UserFilePair> list = owner.getFileListObject().getLendList();

        list.add(new UserFilePair(user, file));

        for (UserFilePair ufp : list) {
            w.writeLine(ufp.compile());
        }

        w.close();
    }

}

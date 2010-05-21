package de.fhma.ss10.srn.tischbein.core.db;

import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.Utils;
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
            if (this.users.containsKey(name.toLowerCase())) {
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

    public Vector<User> getUsers() {
        return new Vector<User>(this.users.values());
    }

    public Vector<User> getUsers(final User without) {
        Vector<User> users = this.getUsers();

        users.remove(without);

        return users;
    }

    public void grantAccess(final User user, final FileItem file) throws DatabaseException {
        if ((file == null) || (user == null)) {
            return;
        }

        try {
            RSAAppender.appendLine(Tables.AccessTable.getFilename(user), user.getKeyPair().getPublic(), file.getId()
                    + DatabaseModel.SEPARATOR + Utils.toHexString(file.getKey()));

            User owner = file.getOwner();
            AESWriter w = AESWriter.createWriter(Tables.LendTable.getFilename(owner), owner.getCryptKey());

            Vector<UserFilePair> list = owner.getFileListObject().getLendList();

            list.add(new UserFilePair(user, file));

            for (UserFilePair ufp : list) {
                w.writeLine(ufp.compile());
            }

            w.close();
        } catch (Exception e) {
            throw new DatabaseException("Kann Recht nicht speichern!", e);
        }

        this.shutdown();
    }

    public boolean hasUser(final String name) {
        return this.users.containsKey(name.toLowerCase());
    }

    /**
     * Fährt die Datenbank runter und speichert noch ausstehende Daten ab.
     */
    public void shutdown() {
        DatabaseStructure.LOCK.lock();

        try {
            Database.instance = null;
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    void addFile(final User user, final FileItem item) throws DatabaseException {
        try {
            // in Dateitabelle des Benutzers Eintrag schreiben
            DatabaseModel.updateUserTables(user, item);

            // in globale Dateitablle Eintrag schreiben
            DatabaseModel.updateGlobalTable(item);

            this.shutdown();
        } catch (Exception e) {
            throw new DatabaseException("Die Datei kann nicht hinzugefügt werden!", e);
        }
    }

}

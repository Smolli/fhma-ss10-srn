package de.fhma.ss10.srn.tischbein.core.db;

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
        Database.LOCK.lock();

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
            Database.LOCK.unlock();
        }
    }

    /**
     * Fährt die Datenbank runter und speichert noch ausstehende Daten ab.
     */
    public static void shutdown() {
        Database.LOCK.lock();

        try {
            Database.instance = null;
        } finally {
            Database.LOCK.unlock();
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
        Database.LOCK.lock();

        try {
            Database db = new Database();

            if (new java.io.File(DB_USERS_TB).createNewFile()) {
                System.out.println("User-Tabelle angelegt.");
            }

            if (new java.io.File(DB_FILES_TB).createNewFile()) {
                System.out.println("Dateien-Tabelle anegelegt.");
            }

            db.loadUsers();
            db.loadFiles();

            return db;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        } finally {
            Database.LOCK.unlock();
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
        Database.LOCK.lock();

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
            Database.LOCK.unlock();
        }
    }

    public boolean hasUser(String name) {
        return this.users.containsKey(name.toLowerCase());
    }

    static void addFile(User user, FileItem item) throws DatabaseException {
        try {
            // in Dateitabelle des Benutzers Eintrag schreiben
            Database.updateUserTables(user, item);

            // in globale Dateitablle Eintrag schreiben
            Database.updateGlobalTable(item);
        } catch (Exception e) {
            throw new DatabaseException("Die Datei kann nicht hinzugefügt werden!", e);
        }
    }

    //    /**
    //     * Versucht einen Benutzer anhand der übergebenen Parameter einzuloggen. Stimmen Benutzername und -passwort mit
    //     * einem Eintrag in der Usertabelle überein, so wird der betreffende Benutzer geladen und sein Objekt zurück
    //     * gegeben. Können die Daten nicht verifiziert werden, so wird eine <code>DatabaseException</code> geworfen.
    //     * 
    //     * @param name
    //     *            Der Benutzername.
    //     * @param pass
    //     *            Das Benutzerpasswort.
    //     * @return Wenn der Benutzername gültig ist und die Passwörter übereinstimmen, wird das ermittelte Benutzer-Objekt
    //     *         zurückgegebe.
    //     * @throws DatabaseException
    //     *             Wird geworfen, wenn der Benutzer nicht angemeldet werden kann.
    //     */
    //    public User loginUser(final String name, final String pass) throws DatabaseException {
    //        try {
    //            User user = this.getUser(name);
    //
    //            if (user == null) {
    //                throw new DatabaseException("Benutzer existiert nicht!");
    //            }
    //
    //            if (!user.unlock(pass)) {
    //                throw new DatabaseException("Falsches Passwort!");
    //            }
    //
    //            return user;
    //        } catch (Exception e) {
    //            throw new DatabaseException("Fehler beim Einloggen!", e);
    //        }
    //    }

    //    public void logoutUser(User user) {
    //        user.lock();
    //    }
}

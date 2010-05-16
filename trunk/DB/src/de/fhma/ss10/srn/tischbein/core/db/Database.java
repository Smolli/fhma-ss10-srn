package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import de.fhma.ss10.srn.tischbein.core.db.FileListObject.UserFilePair;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database {

    /** Standard-Datei für die User-Tabelle. */
    private static final String DB_USERS_TB = "db/users.tb";
    /** Standard-Datei für die Datei-Tabelle. */
    private static final String DB_FILES_TB = "db/files.tb";
    /** Das Reentrantlock. */
    private static final ReentrantLock LOCK = new ReentrantLock();
    /** CSV-Separator. */
    static final char SEPARATOR = ';';
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

            db.loadUsers();
            db.loadFiles();

            return db;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        } finally {
            Database.LOCK.unlock();
        }
    }

    /** Enthält alle bekannten Benutzer in einer Map. Die Information ist öffentlich zugänglich. */
    private TreeMap<String, User> users = new TreeMap<String, User>();
    /** Hält alle bekannten Dateien in einer Map. Die Information ist öffentlich zugänglich. */
    private TreeMap<Integer, File> files = new TreeMap<Integer, File>();

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

            this.saveToUsers(user, pass);
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Anlegen des neuen Benutzers!", e);
        } finally {
            Database.LOCK.unlock();
        }
    }

    /**
     * Gibt ein {@link FileListObject} zurück, das alle eigenen und fremden Dateien enthält, auf den der übergebene
     * Benutzer Zugriff hat.
     * 
     * @param user
     *            Der Benutzer.
     * @return Das {@link FileListObject} mit den Dateien.
     */
    public FileListObject getFileList(final User user) {
        Database.LOCK.lock();

        try {
            FileListObject flo = new FileListObject();

            flo.setFilesTable(this.loadFilesTable(user));
            flo.setAccessTable(this.loadAccessTable(user));
            flo.setLendTable(this.loadLendTable(user));

            return flo;
        } finally {
            Database.LOCK.unlock();
        }
    }

    /**
     * Gibt einen <code>Vector</code> mit allen bekannten Benutzernamen zurück.
     * 
     * @param user
     * @deprecated Diese Methode wird demnächst ersetzt. Ziel ist es ein <code>ListModel</code> zu erzeugen, in dem Alle
     *             Benutzernamen und der Wert des Zuordnungshäkchens gespeichert ist.
     * @return Der Vector mit allen Benutzernamen.
     */
    @Deprecated
    public Vector<String> getUserList() {
        return new Vector<String>(this.users.keySet());
    }

    /**
     * Versucht einen Benutzer anhand der übergebenen Parameter einzuloggen. Stimmen Benutzername und -passwort mit
     * einem Eintrag in der Usertabelle überein, so wird der betreffende Benutzer geladen und sein Objekt zurück
     * gegeben. Können die Daten nicht verifiziert werden, so wird eine <code>DatabaseException</code> geworfen.
     * 
     * @param name
     *            Der Benutzername.
     * @param pass
     *            Das Benutzerpasswort.
     * @return Wenn der Benutzername gültig ist und die Passwörter übereinstimmen, wird das ermittelte Benutzer-Objekt
     *         zurückgegebe.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht angemeldet werden kann.
     */
    public User loginUser(final String name, final String pass) throws DatabaseException {
        try {
            User user = this.users.get(name.toLowerCase());

            if (user == null) {
                throw new DatabaseException("Benutzer existiert nicht!");
            }

            if (!user.unlock(pass)) {
                throw new DatabaseException("Falsches Passwort!");
            }

            return user;
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Einloggen!", e);
        }
    }

    /**
     * Lädt die Zugriffsrechte eines Benutzer und gibt die Dateien zurück, auf die der Benutzer Zugriff hat.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link List} mit allen Dateien.
     */
    private List<File> loadAccessTable(final User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Lädt die Dateientabelle.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    private void loadFiles() throws DatabaseException {
        Database.LOCK.lock();

        try {
            BufferedReader br = new BufferedReader(new FileReader(Database.DB_FILES_TB));
            TreeMap<Integer, File> temp = new TreeMap<Integer, File>();
            String line;

            while ((line = br.readLine()) != null) {
                File file = File.parse(line);

                temp.put(file.getId(), file);
            }

            br.close();

            this.files = temp;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Dateientabelle nicht laden!", e);
        } finally {
            Database.LOCK.unlock();
        }
    }

    /**
     * Lädt die Dateien, die dem Benutzer gehören.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link List} mit allen IDs.
     */
    private List<File> loadFilesTable(final User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Lädt die Tupel aus Benutzer und Datei, die an andere Benutzer verliehen wurden.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link List} mit allen Tupeln.
     */
    private List<UserFilePair> loadLendTable(final User user) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Lädt die Benutzertabelle. Im Fehlerfall bleiben die geladenen Benutzer unverändert.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht vollständig geladen werden kann.
     */
    private void loadUsers() throws DatabaseException {
        Database.LOCK.lock();

        try {
            BufferedReader br = new BufferedReader(new FileReader(Database.DB_USERS_TB));
            TreeMap<String, User> temp = new TreeMap<String, User>();
            String line;

            while ((line = br.readLine()) != null) {
                User user = User.parse(line);

                temp.put(user.getName().toLowerCase(), user);
            }

            br.close();

            this.users = temp;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Benutzertabelle nicht laden!", e);
        } finally {
            Database.LOCK.unlock();
        }
    }

    /**
     * Speichert einen User an das Ende der Benutzertabelle.
     * 
     * @param user
     *            Das Benutzerobjekt.
     * @param pass
     *            Das Benutzerpasswort mit dem der private Schlüssel verschlüsselt wird.
     * @throws DatabaseException
     *             Wird geworfen, wenn der neue Benutzer nicht zur Benutertabelle hinzugefügt werden konnte.
     */
    private void saveToUsers(final User user, final String pass) throws DatabaseException {
        Database.LOCK.lock();

        try {
            FileWriter fw = new FileWriter(Database.DB_USERS_TB, true);

            fw.append(user.compile(pass));

            fw.flush();
            fw.close();

            this.loadUsers();
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        } finally {
            Database.LOCK.unlock();
        }
    }

}

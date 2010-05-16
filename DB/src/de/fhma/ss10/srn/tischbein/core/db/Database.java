package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AESReader;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.db.FileListObject.UserFilePair;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database {

    /**
     * Alle möglichen Tabellen (Helferklasse).
     * 
     * @author Smolli
     */
    private enum Tables {
        /** Die Tabelle mit den Dateien anderer Benutzer, auf die der Benutzer Zugang hat. */
        AccessTable,
        /** Die Tabelle mit den Tupeln, welche Datei der Benutzer anderen Benutzern zugänglich gemacht hat. */
        LendTable,
        /** Die Tabelle mit den Dateien des Benutzers. */
        FileTable;

        /**
         * Gibt den Dateinamen der Tabelle im Benutzerkontext zurück.
         * 
         * @param user
         *            Der {@link User}-Kontext.
         * @return Der eindeutige Dateiname.
         */
        public String getFilename(final User user) {
            StringBuilder sb = new StringBuilder("db/users/");

            sb.append(Utils.toMD5Hex(user.getName()));

            switch (this) {
                case FileTable:
                    sb.append(".files");
                    break;

                case AccessTable:
                    sb.append(".access");
                    break;

                case LendTable:
                    sb.append(".lend");
                    break;

                default:
                    throw new RuntimeException("Gnarf!");
            }

            sb.append(".tb");

            return sb.toString();
        }

    }

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

            new java.io.File(Database.DB_USERS_TB).createNewFile();
            new java.io.File(Database.DB_FILES_TB).createNewFile();

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

            this.createUserFiles(user);

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
     * @throws CryptoException
     *             Wird geworfen, wenn eine der Tabellen nicht entschlüsselt werden konnte.
     * @throws DatabaseException
     *             Wird geworfen, wenn eine der Tabellen nicht geladen werden konnte.
     */
    public FileListObject getFileList(final User user) throws CryptoException, DatabaseException {
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
     * Erstellt die Tabellen-Dateien für die Datenbank.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @throws IOException
     *             Wird geworfen, wenn eine der Dateien nicht erstellt werden konnte.
     * @throws DatabaseException
     *             Wird geworfen, wenn eine der Dateien nicht erstellt werden konnte.
     */
    private void createUserFiles(final User user) throws IOException, DatabaseException {
        new java.io.File(Tables.FileTable.getFilename(user)).createNewFile();
        new java.io.File(Tables.AccessTable.getFilename(user)).createNewFile();
        new java.io.File(Tables.LendTable.getFilename(user)).createNewFile();
    }

    /**
     * Gibt eine Datei aus der Map zurück und prüft vorher, ob sie existiert.
     * 
     * @param id
     *            Die ID der Datei.
     * @return Gibt das {@link File}-Objekt der Datei zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei mit der ID nicht im System ist.
     */
    private File getFile(final int id) throws DatabaseException {
        if (!this.files.containsKey(id)) {
            throw new DatabaseException("Datei ist nicht bekannt!");
        }

        return this.files.get(id);
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
     * @throws CryptoException
     *             Wird geworfen, wenn die Tabelle nicht entschlüsselt werden kann.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden kann.
     */
    private List<File> loadFilesTable(final User user) throws CryptoException, DatabaseException {
        AESReader br = AESReader.createReader(Tables.FileTable.getFilename(user), user.getCryptKey());
        String line;
        List<File> list = new ArrayList<File>();

        try {
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";");

                int id = Integer.parseInt(cols[0]);
                File file = this.getFile(id);

                file.setKey(Utils.fromHexString(cols[1]));

                list.add(file);
            }

            return list;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Dateientabelle nicht laden!", e);
        }
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

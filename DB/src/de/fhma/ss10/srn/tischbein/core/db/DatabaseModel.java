package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AESReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AESWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.crypto.RSAReader;
import de.fhma.ss10.srn.tischbein.core.db.FileListObject.UserFilePair;

public class DatabaseModel extends DatabaseStructure {

    /**
     * Alle möglichen Tabellen (Helferklasse).
     * 
     * @author Smolli
     */
    protected enum Tables {
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

    /** CSV-Separator. */
    protected static final String SEPARATOR = ";";

    /**
     * Aktualisiert die globale Dateien-Tabelle.
     * 
     * @param fi
     *            Das {@link FileItem}, mit dem die Tabelle ergänzt werden soll.
     * @throws IOException
     *             Wird geworfen, wenn die Tabelle nicht erweitert werden kann.
     */
    protected static void updateGlobalTable(final FileItem fi) throws IOException {
        FileWriter fw = new FileWriter(DatabaseStructure.DB_FILES_TB, true);

        fw.write(fi.compile());

        fw.close();
    }

    /**
     * Aktualisiert die Benutzertabellen.
     * 
     * @param user
     *            Der Benutzer, dessen Tabellen aktualisiert werden sollen.
     * @param fi
     *            Das {@link FileItem}, das hinzugefügt werden soll.
     * @throws IOException
     *             Wird geworfen, wenn die Tabellen nicht erweitert werden konnten.
     */
    protected static void updateUserTables(final User user, final FileItem fi) throws IOException {
        AESWriter w;
        FileListObject flo = user.getFileListObject();

        flo.getFileList().add(fi);

        w = AESWriter.createWriter(Tables.FileTable.getFilename(user), user.getCryptKey());

        for (FileItem item : flo.getFileList()) {
            w.writeLine(Integer.toString(item.getId()) + DatabaseModel.SEPARATOR + Utils.toHexString(item.getKey()));
        }

        w.close();
    }

    /** Enthält alle bekannten Benutzer in einer Map. Die Information ist öffentlich zugänglich. */
    protected TreeMap<String, User> users = new TreeMap<String, User>();

    /** Hält alle bekannten Dateien in einer Map. Die Information ist öffentlich zugänglich. */
    private TreeMap<Integer, FileItem> files = new TreeMap<Integer, FileItem>();

    /** Hält die höchste vergebene Datei ID. */
    protected int lastFileId;

    public DatabaseModel() {
        super();
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
        if (!this.users.containsKey(name.toLowerCase())) {
            throw new DatabaseException("Benutzer ist nicht bekannt!");
        }

        return this.users.get(name.toLowerCase());
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
    protected FileListObject getFileList(final User user) throws CryptoException, DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            FileListObject flo = new FileListObject();

            flo.setFilesTable(this.loadFilesTable(user));
            flo.setAccessTable(this.loadAccessTable(user));
            flo.setLendTable(this.loadLendTable(user));

            return flo;
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    /**
     * Gibt die nächte Datei ID zurück. Die IDs werden Datenbankweit vergeben und sind eindeutig.
     * 
     * @return Die ID als {@link Integer}.
     */
    protected int getNextFileId() {
        return this.lastFileId + 1;
    }

    /**
     * Lädt die Dateientabelle.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    protected void loadFiles() throws DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            BufferedReader br = new BufferedReader(new FileReader(DatabaseStructure.DB_FILES_TB));
            TreeMap<Integer, FileItem> temp = new TreeMap<Integer, FileItem>();
            String line;

            while ((line = br.readLine()) != null) {
                FileItem file = FileItem.parse(null, line);
                int id = file.getId();

                temp.put(id, file);

                if (id > this.lastFileId) {
                    this.lastFileId = id;
                }
            }

            br.close();

            this.files = temp;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Dateientabelle nicht laden!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    /**
     * Lädt die Benutzertabelle. Im Fehlerfall bleiben die geladenen Benutzer unverändert.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht vollständig geladen werden kann.
     */
    protected void loadUsers() throws DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            BufferedReader br = new BufferedReader(new FileReader(DatabaseStructure.DB_USERS_TB));
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
            DatabaseStructure.LOCK.unlock();
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
    protected void saveToUsers(final User user, final String pass) throws DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            FileWriter fw = new FileWriter(DatabaseStructure.DB_USERS_TB, true);

            fw.append(user.compile(pass));

            fw.flush();
            fw.close();

            this.loadUsers();
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
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
    private FileItem getFile(final int id) throws DatabaseException {
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
     * @throws DatabaseException
     */
    private Vector<FileItem> loadAccessTable(final User user) throws DatabaseException {
        try {
            Vector<FileItem> list = new Vector<FileItem>();
            RSAReader r = RSAReader.createReader(Tables.AccessTable.getFilename(user), user.getKeyPair().getPrivate());
            String line;

            while ((line = r.readLine()) != null) {
                String[] cols = line.split(DatabaseModel.SEPARATOR);
                FileItem file = this.getFile(Integer.parseInt(cols[0]));

                file.setKey(Utils.fromHexString(cols[1]));

                list.add(file);
            }

            return list;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Access-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Dateien, die dem Benutzer gehören.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link Vector} mit allen IDs.
     * @throws CryptoException
     *             Wird geworfen, wenn die Tabelle nicht entschlüsselt werden kann.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden kann.
     */
    private Vector<FileItem> loadFilesTable(final User user) throws CryptoException, DatabaseException {
        AESReader br = AESReader.createReader(Tables.FileTable.getFilename(user), user.getCryptKey());
        String line;
        Vector<FileItem> list = new Vector<FileItem>();

        try {
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";");

                int id = Integer.parseInt(cols[0]);
                FileItem file = this.getFile(id);

                file.setKey(Utils.fromHexString(cols[1]));
                file.setOwner(user);

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
     * @return Eine {@link Vector} mit allen Tupeln.
     * @throws CryptoException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    private Vector<UserFilePair> loadLendTable(final User user) throws CryptoException, DatabaseException {
        AESReader br = AESReader.createReader(Tables.LendTable.getFilename(user), user.getCryptKey());
        String line;
        Vector<UserFilePair> list = new Vector<UserFilePair>();

        try {
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";");

                String userName = cols[0];
                int fileId = Integer.parseInt(cols[1]);

                FileItem fileObject = this.getFile(fileId);
                User userObject = this.getUser(userName);

                list.add(new UserFilePair(userObject, fileObject));
            }

            return list;
        } catch (Exception e) {
            throw new DatabaseException("Kann die Dateientabelle nicht laden!", e);
        }
    }

}

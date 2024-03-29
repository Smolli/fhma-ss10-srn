package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaAppender;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePair;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePairVector;

/**
 * Stellt das Datenbank-Modell dar und spezialisiert somit die Datenbankstruktur.
 * 
 * @author Smolli
 */
public abstract class DatabaseModel extends DatabaseStructure {

    /** Enthält alle bekannten Benutzer in einer Map. Die Information ist öffentlich zugänglich. */
    private final TreeMap<String, User> users = new TreeMap<String, User>();

    /** Hält alle bekannten Dateien in einer Map. Die Information ist öffentlich zugänglich. */
    private final TreeMap<Integer, FileItem> files = new TreeMap<Integer, FileItem>();

    /** Hält die höchste vergebene Datei ID. */
    private int lastFileId;

    /**
     * Geschützter Ctor.
     */
    protected DatabaseModel() {
        super();
    }

    /**
     * Aktualisiert die globale Dateien-Tabelle.
     * 
     * @param fi
     *            Das {@link FileItem}, mit dem die Tabelle ergänzt werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht erweitert werden kann.
     */
    protected void addFileToGlobalTable(final FileItem fi) throws DatabaseException {
        try {
            this.files.put(fi.getId(), fi);

            this.writeFilesTable(new Vector<FileItem>(this.files.values()));
        } catch (Exception e) {
            throw new DatabaseException("Kann die Tabelle nicht ändern!", e);
        }
    }

    /**
     * Aktualisiert die Benutzertabellen.
     * 
     * @param fi
     *            Das {@link FileItem}, das hinzugefügt werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabellen nicht erweitert werden konnten.
     */
    protected void addFileToUserTable(final FileItem fi) throws DatabaseException {
        try {
            User owner = fi.getOwner();
            UserDescriptor flo = owner.getDescriptor();

            flo.getFileList().add(fi);

            this.writeUserFilesTable(owner);
        } catch (Exception e) {
            throw new DatabaseException("Kann die Tabelle nicht updaten!", e);
        }
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
    protected void addRemarkToOwner(final User user, final FileItem file) throws IOException {
        User owner = file.getOwner();
        AesWriter w = AesWriter.createWriter(DatabaseTables.LendTable.getFilename(owner), owner.getCryptKey());

        Vector<UserFilePair> list = owner.getDescriptor().getLendList();

        list.add(new UserFilePair(user, file));

        for (UserFilePair ufp : list) {
            w.writeLine(ufp.compile());
        }

        w.close();
    }

    /**
     * Fügt einen neuen Benutzer zum Modell hinzu.
     * 
     * @param user
     *            Der Benutzer.
     * @param pass
     *            Das Benutzerpasswort.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Benutzer nicht angelegt werden konnte.
     */
    protected void addUser(final User user, final String pass) throws DatabaseException {
        try {
            this.createUserFiles(user);

            this.appendUserToUsersTable(user, pass);
        } catch (Exception e) {
            throw new DatabaseException("Kann den Benutzer nicht hinzufügen!", e);
        } finally {
            Database.getInstance().shutdown();
        }
    }

    /**
     * Entzieht dem angegebenen Benutzer das Recht für die angegebene Datei.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @throws DatabaseException
     *             Wird geworfen, wenn das Recht nicht entzogen werden konnte.
     */
    protected void denyAccessToUser(final User user, final FileItem file) throws DatabaseException {
        try {
            //            String filename = DatabaseTables.AccessTable.getFilename(user);
            Vector<String> lines = this.rawReadAccessTable(user);

            lines = this.removeAccess(file, lines);

            this.writeAccessTable(user, lines);

        } catch (Exception e) {
            throw new DatabaseException("Kann die Access-Tabelle nicht bearbeiten!", e);
        }
    }

    /**
     * Gibt die Datei-Map zurück.
     * 
     * @return Die Datei-Map.
     */
    protected TreeMap<Integer, FileItem> getFileMap() {
        return this.files;
    }

    /**
     * Gibt die höchste Datei-ID zurück.
     * 
     * @return Die höchste Datei-ID.
     */
    protected int getLastFileId() {
        return this.lastFileId;
    }

    /**
     * Gibt die Benutzer-Map zurück.
     * 
     * @return Die Benutzer-Map.
     */
    protected TreeMap<String, User> getUserMap() {
        return this.users;
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
    protected void grantAccessToUser(final User user, final FileItem file) throws UtilsException {
        String filename = DatabaseTables.AccessTable.getFilename(user);
        String messge = Utils.serializeKeyHex(file.getKey());

        RsaAppender.appendLine(filename, user.getPublicKey(), messge, Integer.toString(file.getId()));
    }

    /**
     * Öffnet ein bestehendes Datenbankschema.
     * 
     * @param db
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datenbankstruktur korrupt ist oder die Datenbank aus anderen Gründen nicht
     *             geladen werden konnte.
     */
    protected void open() throws DatabaseException {
        DatabaseFiles.LOCK.lock();

        try {
            this.testBaseStructure();

            this.fetchUsers();

            this.fetchFiles();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

    /**
     * Entfernt das Zugriffsrecht für alle Benutzer, denen das Recht zugewiesen wurde, aus der Datenbank.
     * 
     * @param item
     *            Die Datei.
     * @throws DatabaseException
     *             Wird geworfen, wenn das Recht nicht allen beteiligten Benutzern entzogen werden konnte.
     */
    protected void removeFileFromAccessTables(final FileItem item) throws DatabaseException {
        User owner = item.getOwner();
        Vector<User> deptors = owner.getDescriptor().getLendList().getDeptors(item);

        for (User user : deptors) {
            Database.getInstance().denyAccess(user, item);
        }
    }

    /**
     * Entfernt den Dateieintrag aus der globalen Dateientabelle.
     * 
     * @param item
     *            Die Datei.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei nicht entfernt werden konnte.
     */
    protected void removeFileFromGlobalTable(final FileItem item) throws DatabaseException {
        this.files.remove(item.getId());

        this.writeFilesTable(new Vector<FileItem>(this.files.values()));
    }

    /**
     * Entfernt die Datei aus der Datei-Tabelle des Besitzers.
     * 
     * @param item
     *            Die Datei
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei nicht entfernt werden konnte.
     */
    protected void removeFileFromOwnerTable(final FileItem item) throws DatabaseException {
        User owner = item.getOwner();

        owner.getDescriptor().getFileList().remove(item);

        this.writeUserFilesTable(owner);
    }

    /**
     * Entfernt den Leihverweis aus der Lend-Tabelle des Benutzers.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Datei nicht entfernt werden konnte.
     */
    protected void removeRemarkFromOwner(final User user, final FileItem file) throws DatabaseException {
        try {
            User owner = file.getOwner();
            AesWriter w = AesWriter.createWriter(DatabaseTables.LendTable.getFilename(owner), owner.getCryptKey());

            UserFilePairVector list = owner.getDescriptor().getLendList();

            list.remove(new UserFilePair(user, file));

            for (UserFilePair ufp : list) {
                w.writeLine(ufp.compile());
            }

            w.close();
        } catch (Exception e) {
            throw new DatabaseException("Kann Datei nicht entfernen!", e);
        }
    }

    /**
     * Ermittelt die Dateien.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Dateien nicht ermittelt werden können.
     */
    private void fetchFiles() throws DatabaseException {
        for (FileItem file : this.loadFilesTable()) {
            int id = file.getId();

            if (id > this.lastFileId) {
                this.lastFileId = id;
            }

            this.files.put(file.getId(), file);
        }
    }

    /**
     * Ermittelt die Benutzer.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Benutzer nicht ermittelt werden können.
     */
    private void fetchUsers() throws DatabaseException {
        for (User user : this.loadUsersTable()) {
            this.users.put(user.getName().toLowerCase(), user);
        }
    }

    /**
     * Ließt die Access-Tabelle im Rohformat aus.
     * 
     * @param user
     *            Der Dateiname der Access-Tabelle.
     * @return Gibt die Tabelle im Rohformat zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabellen nicht gelesen werden konnte.
     */
    private Vector<String> rawReadAccessTable(final User user) throws DatabaseException {
        try {
            return (new DatabaseTableReader<String>(Utils.createBufferedReader(DatabaseTables.AccessTable
                    .getFilename(user))) {

                @Override
                protected String process(final String line) throws Exception {
                    return line;
                }

            }).getResult();
        } catch (Exception e) {
            throw new DatabaseException("Kann Access-Tabelle nicht lesen!", e);
        }
    }

    /**
     * Entfernt die Zugriffsberechtigung aus den Rohdaten der Access-Tabelle.
     * 
     * @param file
     *            Die Datei, deren Zugriffsberechtigung entfernt werden soll.
     * @param lines
     *            Die Rohdaten der Tabelle.
     * @return Gibt die bereinigten Rohdaten zurück.
     */
    private Vector<String> removeAccess(final FileItem file, final Vector<String> lines) {
        Vector<String> temp = new Vector<String>();

        for (String line : lines) {
            String[] cols = line.split(DatabaseStructure.SEPARATOR);

            if (Integer.parseInt(cols[0]) != file.getId()) {
                temp.add(line);
            }
        }

        return temp;
    }

}

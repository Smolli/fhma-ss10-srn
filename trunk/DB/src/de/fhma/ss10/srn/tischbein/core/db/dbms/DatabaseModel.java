package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.UtilsException;
import de.fhma.ss10.srn.tischbein.core.crypto.AesWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaAppender;
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

    private List<String> readAccessTable(final FileItem file, final String filename) throws FileNotFoundException,
            IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<String>();
        String line;

        while ((line = br.readLine()) != null) {
            String[] cols = line.split(DatabaseStructure.SEPARATOR);

            if (Integer.parseInt(cols[0]) != file.getId()) {
                lines.add(line);
            }
        }

        br.close();

        return lines;
    }

    private void writeAccessTable(final String filename, final List<String> lines) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

        for (String line : lines) {
            bw.write(line);
        }

        bw.flush();
        bw.close();
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

            this.saveToUsers(user, pass);
        } catch (Exception e) {
            throw new DatabaseException("Kann den Benutzer nicht hinzufügen!", e);
        }
    }

    protected void denyAccessToUser(final User user, final FileItem file) throws DatabaseException {
        try {
            String filename = DatabaseTables.AccessTable.getFilename(user);
            List<String> lines = this.readAccessTable(file, filename);

            this.writeAccessTable(filename, lines);

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
        DatabaseStructure.LOCK.lock();

        try {
            this.testBaseStructure();

            this.fetchUsers();

            this.fetchFiles();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datenbankstruktur nicht laden!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

    protected void removeRemarkFromOwner(final User user, final FileItem file) throws IOException {
        User owner = file.getOwner();
        AesWriter w = AesWriter.createWriter(DatabaseTables.LendTable.getFilename(owner), owner.getCryptKey());

        UserFilePairVector list = owner.getDescriptor().getLendList();

        list.remove(new UserFilePair(user, file));

        for (UserFilePair ufp : list) {
            w.writeLine(ufp.compile());
        }

        w.close();
    }

    /**
     * Aktualisiert die globale Dateien-Tabelle.
     * 
     * @param fi
     *            Das {@link FileItem}, mit dem die Tabelle ergänzt werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht erweitert werden kann.
     */
    protected void updateGlobalTable(final FileItem fi) throws DatabaseException {
        try {
            FileWriter fw = new FileWriter(DatabaseFiles.DB_FILES_TB, true);

            fw.write(fi.compile());

            fw.close();
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
    protected void updateUserTables(final FileItem fi) throws DatabaseException {
        try {
            UserDescriptor flo = fi.getOwner().getDescriptor();

            flo.getFileList().add(fi);

            new DatabaseTableWriter<FileItem>(AesWriter.createWriter(DatabaseTables.FileTable
                    .getFilename(fi.getOwner()), fi.getOwner().getCryptKey()), flo.getFileList()) {

                @Override
                protected String process(final FileItem item) throws Exception {
                    return Integer.toString(item.getId()) + DatabaseStructure.SEPARATOR
                            + Utils.serializeKeyHex(item.getKey());
                }

            };
        } catch (Exception e) {
            throw new DatabaseException("Kann die Tabelle nicht updaten!", e);
        }
    }

}

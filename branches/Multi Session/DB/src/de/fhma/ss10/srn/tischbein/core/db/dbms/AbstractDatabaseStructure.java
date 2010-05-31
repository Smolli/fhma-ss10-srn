package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.security.PrivateKey;
import java.util.List;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AesReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AesWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaReader;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePairList;

/**
 * Enthält die Datenbankstruktur.
 * 
 * @author Smolli
 */
public abstract class AbstractDatabaseStructure extends DatabaseIO {

    /** CSV-Separator. */
    public static final String SEPARATOR = ";";

    /**
     * Geschützter Ctor.
     */
    protected AbstractDatabaseStructure() {
        super();
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
    protected void appendUserToUsersTable(final User user, final String pass) throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            final BufferedWriter writer = Utils.createBufferedWriter(DatabaseIO.DB_USERS_TB, true);

            writer.append(user.compile(pass));
            writer.append("\n");

            writer.flush();
            writer.close();
        } catch (final Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Ließt die eigentliche Dateien-Tabelle aus.
     * 
     * @return Gibt die {@link FileItem} als {@link List} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    protected List<FileItem> loadFilesTable() throws DatabaseException {
        try {
            final BufferedReader reader = Utils.createBufferedReader(DatabaseIO.DB_FILES_TB);

            final FilesTableReader tableReader = new FilesTableReader(reader);

            return tableReader.getResult();
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Dateien-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Zugriffsrechte eines Benutzer und gibt die Dateien zurück, auf die der Benutzer Zugriff hat.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link List} mit allen Dateien.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Access-Tabelle nicht geladen werden konnte.
     */
    protected List<FileItem> loadUserAccessTable(final User user) throws DatabaseException {
        try {
            final String filename = DatabaseTables.AccessTable.getFilename(user);
            final PrivateKey privateKey = user.getPrivateKey();
            final RsaReader reader = RsaReader.createReader(filename, privateKey);

            final UserAccessTableReader tableReader = new UserAccessTableReader(reader);

            return tableReader.getResult();
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Access-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Dateien, die dem Benutzer gehören.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link List} mit allen IDs.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden kann.
     */
    protected List<FileItem> loadUserFilesTable(final User user) throws DatabaseException {
        try {
            final AesReader reader = AesReader.createReader(DatabaseTables.FileTable.getFilename(user), user
                    .getCryptKey());

            final UserFilesTableReader tabelReader = new UserFilesTableReader(reader, user);

            return tabelReader.getResult();
        } catch (final Exception e) {
            throw new DatabaseException("Kann die User-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Tupel aus Benutzer und Datei, die an andere Benutzer verliehen wurden.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link UserFilePairList} mit allen Tupeln.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    protected UserFilePairList loadUserLendTable(final User user) throws DatabaseException {
        try {
            final AesReader reader = AesReader.createReader(DatabaseTables.LendTable.getFilename(user), user
                    .getCryptKey());

            final UserLendTableReader tableReader = new UserLendTableReader(reader);

            return new UserFilePairList(tableReader.getResult());
        } catch (final Exception e) {
            throw new DatabaseException("Kann nicht die Lend-Tabelle laden!", e);
        }
    }

    /**
     * Lädt die Benutzertabelle. Im Fehlerfall bleiben die geladenen Benutzer unverändert.
     * 
     * @return Gibt die Benutzer als {@link List} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht vollständig geladen werden kann.
     */
    protected List<User> loadUsersTable() throws DatabaseException {
        DatabaseIO.LOCK.lock();

        try {
            final BufferedReader reader = Utils.createBufferedReader(DatabaseIO.DB_USERS_TB);

            final UsersTableReader tableReader = new UsersTableReader(reader);

            return tableReader.getResult();
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Benutzertabelle nicht laden!", e);
        } finally {
            DatabaseIO.LOCK.unlock();
        }
    }

    /**
     * Schreibt die Access-Tabelle des Benutzers.
     * 
     * @param user
     *            Der Benutzer-Kontext.
     * @param lines
     *            Die Rohdaten der Tabelle.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden kann.
     */
    protected void writeAccessTable(final User user, final List<String> lines) throws DatabaseException {
        try {
            final BufferedWriter writer = Utils.createBufferedWriter(DatabaseTables.AccessTable.getFilename(user),
                    false);

            new UserAccessTableWriter(writer, lines);
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Access-Tabelle nicht schreiben!", e);
        }
    }

    /**
     * Schreibt die globale Files-Tabelle.
     * 
     * @param list
     *            Die Elemente der Tabelle.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden kann.
     */
    protected void writeFilesTable(final List<FileItem> list) throws DatabaseException {
        try {
            final BufferedWriter writer = Utils.createBufferedWriter(DatabaseIO.DB_FILES_TB, false);

            new FilesTableWriter(writer, list);
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Files-Tabelle nicht schreiben.", e);
        }
    }

    /**
     * Schreibt die Files-Tabelle des Benutzers.
     * 
     * @param user
     *            Der Benutzer.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden kann.
     */
    protected void writeUserFilesTable(final User user) throws DatabaseException {
        final AesWriter writer = AesWriter.createWriter(DatabaseTables.FileTable.getFilename(user), user.getCryptKey());

        new UserFilesTableWriter(writer, user.getDescriptor().getFileList());
    }

}

package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.security.PrivateKey;
import java.util.Vector;

import javax.crypto.SecretKey;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AesReader;
import de.fhma.ss10.srn.tischbein.core.crypto.AesWriter;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaReader;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePair;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePairVector;

/**
 * Enthält die Datenbankstruktur.
 * 
 * @author Smolli
 */
public abstract class DatabaseStructure extends DatabaseFiles {

    /** CSV-Separator. */
    public static final String SEPARATOR = ";";

    /**
     * Geschützter Ctor.
     */
    protected DatabaseStructure() {
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
        DatabaseFiles.LOCK.lock();

        try {
            BufferedWriter fw = Utils.createBufferedWriter(DatabaseFiles.DB_USERS_TB, true);

            fw.append(user.compile(pass));
            fw.append("\n");

            fw.flush();
            fw.close();

            Database.getInstance().shutdown();
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

    /**
     * Ließt die eigentliche Dateien-Tabelle aus.
     * 
     * @return Gibt die {@link FileItem} als {@link Vector} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    protected Vector<FileItem> loadFilesTable() throws DatabaseException {
        try {
            BufferedReader reader = Utils.createBufferedReader(DatabaseFiles.DB_FILES_TB);

            DatabaseTableReader<FileItem> lr = new DatabaseTableReader<FileItem>(reader) {

                @Override
                protected FileItem process(final String line) throws Exception {
                    FileItem file = FileItem.parse(null, line);

                    return file;
                }

            };

            return lr.getResult();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Dateien-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Zugriffsrechte eines Benutzer und gibt die Dateien zurück, auf die der Benutzer Zugriff hat.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link Vector} mit allen Dateien.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Access-Tabelle nicht geladen werden konnte.
     */
    protected Vector<FileItem> loadUserAccessTable(final User user) throws DatabaseException {
        try {
            String filename = DatabaseTables.AccessTable.getFilename(user);
            PrivateKey privateKey = user.getPrivateKey();
            RsaReader reader = RsaReader.createReader(filename, privateKey);

            return (new DatabaseTableReader<FileItem>(reader) {

                @Override
                protected FileItem process(final String line) throws Exception {
                    String[] cols = line.split(DatabaseStructure.SEPARATOR);
                    FileItem file = Database.getInstance().getFile(Integer.parseInt(cols[0]));

                    file.setKey((SecretKey) Utils.deserializeKeyHex(cols[1]));

                    return file;
                }

            }).getResult();
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
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden kann.
     */
    protected Vector<FileItem> loadUserFilesTable(final User user) throws DatabaseException {
        try {
            AesReader reader = AesReader.createReader(DatabaseTables.FileTable.getFilename(user), user.getCryptKey());

            return (new DatabaseTableReader<FileItem>(reader) {

                @Override
                protected FileItem process(final String line) throws Exception {
                    String[] cols = line.split(";");

                    int id = Integer.parseInt(cols[0]);
                    FileItem file = Database.getInstance().getFile(id);

                    file.setKey((SecretKey) Utils.deserializeKeyHex(cols[1]));
                    file.setOwner(user);

                    return file;
                }

            }).getResult();
        } catch (Exception e) {
            throw new DatabaseException("Kann die User-Tabelle nicht laden!", e);
        }
    }

    /**
     * Lädt die Tupel aus Benutzer und Datei, die an andere Benutzer verliehen wurden.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @return Eine {@link UserFilePairVector} mit allen Tupeln.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden konnte.
     */
    protected UserFilePairVector loadUserLendTable(final User user) throws DatabaseException {
        try {
            AesReader reader = AesReader.createReader(DatabaseTables.LendTable.getFilename(user), user.getCryptKey());

            DatabaseTableReader<UserFilePair> lr = new DatabaseTableReader<UserFilePair>(reader) {

                @Override
                protected UserFilePair process(final String line) throws Exception {
                    return UserFilePair.parse(line);
                }

            };

            return new UserFilePairVector(lr.getResult());
        } catch (Exception e) {
            throw new DatabaseException("Kann nicht die Lend-Tabelle laden!", e);
        }
    }

    /**
     * Lädt die Benutzertabelle. Im Fehlerfall bleiben die geladenen Benutzer unverändert.
     * 
     * @return Gibt die Benutzer als {@link Vector} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht vollständig geladen werden kann.
     */
    protected Vector<User> loadUsersTable() throws DatabaseException {
        DatabaseFiles.LOCK.lock();

        try {
            BufferedReader reader = Utils.createBufferedReader(DatabaseFiles.DB_USERS_TB);

            return (new DatabaseTableReader<User>(reader) {

                @Override
                protected User process(final String line) throws Exception {
                    return User.parse(line);
                }
            }).getResult();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Benutzertabelle nicht laden!", e);
        } finally {
            DatabaseFiles.LOCK.unlock();
        }
    }

    /**
     * Schreibt die Access-Tabelle des Benutzers.
     * 
     * @param user
     *            Der Benutzer-Kontext.
     * @param rawData
     *            Die Rohdaten der Tabelle.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden kann.
     */
    protected void writeAccessTable(final User user, final Vector<String> rawData) throws DatabaseException {
        try {
            BufferedWriter writer = Utils.createBufferedWriter(DatabaseTables.AccessTable.getFilename(user), false);

            new DatabaseTableWriter<String>(writer, rawData) {

                @Override
                protected String process(final String item) throws Exception {
                    return item;
                }

            };
        } catch (Exception e) {
            throw new DatabaseException("Kann die Access-Tabelle nicht schreiben!", e);
        }
    }

    /**
     * Schreibt die globale Files-Tabelle.
     * 
     * @param collection
     *            Die Elemente der Tabelle.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden kann.
     */
    protected void writeFilesTable(final Vector<FileItem> collection) throws DatabaseException {
        try {
            BufferedWriter writer = Utils.createBufferedWriter(DatabaseFiles.DB_FILES_TB, false);

            new DatabaseTableWriter<FileItem>(writer, collection) {

                @Override
                protected String process(final FileItem item) throws Exception {
                    return item.compile();
                }

            };
        } catch (Exception e) {
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
        AesWriter writer = AesWriter.createWriter(DatabaseTables.FileTable.getFilename(user), user.getCryptKey());

        new DatabaseTableWriter<FileItem>(writer, user.getDescriptor().getFileList()) {

            @Override
            protected String process(final FileItem item) throws Exception {
                return Integer.toString(item.getId()) + DatabaseStructure.SEPARATOR
                        + Utils.serializeKeyHex(item.getKey());
            }

        };
    }

}

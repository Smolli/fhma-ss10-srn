package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.PrivateKey;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.SecretKey;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AesReader;
import de.fhma.ss10.srn.tischbein.core.crypto.CryptoException;
import de.fhma.ss10.srn.tischbein.core.crypto.RsaReader;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePair;

/**
 * Enthält die Datenbankstruktur.
 * 
 * @author Smolli
 */
public abstract class DatabaseStructure extends DatabaseFiles {

    /** CSV-Separator. */
    public static final String SEPARATOR = ";";
    /** Das Reentrantlock. */
    protected static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * Geschützter Ctor.
     */
    protected DatabaseStructure() {
        super();
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
            DatabaseTableReader<FileItem> lr = new DatabaseTableReader<FileItem>(new BufferedReader(new FileReader(
                    DatabaseFiles.DB_FILES_TB))) {

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
     * @throws CryptoException
     *             Wird geworfen, wenn die Tabelle nicht entschlüsselt werden kann.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geladen werden kann.
     */
    protected Vector<FileItem> loadUserFilesTable(final User user) throws CryptoException, DatabaseException {
        DatabaseTableReader<FileItem> lr = new DatabaseTableReader<FileItem>(AesReader.createReader(
                DatabaseTables.FileTable.getFilename(user), user.getCryptKey())) {

            @Override
            protected FileItem process(final String line) throws Exception {
                String[] cols = line.split(";");

                int id = Integer.parseInt(cols[0]);
                FileItem file = Database.getInstance().getFile(id);

                file.setKey((SecretKey) Utils.deserializeKeyHex(cols[1]));
                file.setOwner(user);

                return file;
            }

        };

        return lr.getResult();
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
    protected Vector<UserFilePair> loadUserLendTable(final User user) throws CryptoException, DatabaseException {
        DatabaseTableReader<UserFilePair> lr = new DatabaseTableReader<UserFilePair>(AesReader.createReader(
                DatabaseTables.LendTable.getFilename(user), user.getCryptKey())) {

            @Override
            protected UserFilePair process(final String line) throws Exception {
                return UserFilePair.parse(line);
            }

        };

        return lr.getResult();
    }

    /**
     * Lädt die Benutzertabelle. Im Fehlerfall bleiben die geladenen Benutzer unverändert.
     * 
     * @return Gibt die Benutzer als {@link Vector} zurück.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht vollständig geladen werden kann.
     */
    protected Vector<User> loadUsersTable() throws DatabaseException {
        DatabaseStructure.LOCK.lock();

        try {
            DatabaseTableReader<User> r = new DatabaseTableReader<User>(new BufferedReader(new FileReader(
                    DatabaseFiles.DB_USERS_TB))) {

                @Override
                protected User process(final String line) throws Exception {
                    return User.parse(line);
                }
            };

            return r.getResult();
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
            FileWriter fw = new FileWriter(DatabaseFiles.DB_USERS_TB, true);

            fw.append(user.compile(pass));

            fw.flush();
            fw.close();

            Database.getInstance().shutdown();
        } catch (Exception e) {
            throw new DatabaseException("Fehler beim Schreiben in die Users-Tabelle!", e);
        } finally {
            DatabaseStructure.LOCK.unlock();
        }
    }

}

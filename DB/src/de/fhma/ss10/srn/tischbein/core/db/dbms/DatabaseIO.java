package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor.UserFilePair;

/**
 * Basisklasse der Datenbank. Hier geht es um das Dateisystem.
 * 
 * @author Smolli
 */
public class DatabaseIO {

    /**
     * Spezialisierter {@link AbstractDatabaseTableReader} zum Lesen einer Files-Tabelle.
     * 
     * @author Smolli
     */
    static final class FilesTableReader extends AbstractDatabaseTableReader<FileItem> {

        /**
         * Ctor.
         * 
         * @param reader
         *            Quell-Reader.
         * @throws DatabaseException
         *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
         */
        public FilesTableReader(final BufferedReader reader) throws DatabaseException {
            super(reader);
        }

        @Override
        protected FileItem process(final String line) {
            final FileItem file = FileItem.parse(null, line);

            return file;
        }

    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableWriter} zum Schreiben der Files-Tabelle.
     * 
     * @author Smolli
     */
    static final class FilesTableWriter extends AbstractDatabaseTableWriter<FileItem> {

        /**
         * Ctor.
         * 
         * @param writer
         *            Der Ziel-Writer.
         * @param items
         *            Die Elemente.
         * @throws DatabaseException
         *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
         */
        public FilesTableWriter(final BufferedWriter writer, final List<FileItem> items) throws DatabaseException {
            super(writer, items);
        }

        @Override
        protected String process(final FileItem item) {
            return item.compile();
        }

    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableReader} zum Lesen einer UserAcccess-Tabelle.
     * 
     * @author Smolli
     */
    static final class UserAccessTableReader extends AbstractDatabaseTableReader<FileItem> {

        /**
         * Ctor.
         * 
         * @param reader
         *            Quell-Reader.
         * @throws DatabaseException
         *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
         */
        public UserAccessTableReader(final BufferedReader reader) throws DatabaseException {
            super(reader);
        }

        @Override
        protected FileItem process(final String line) {
            FileItem result = null;

            try {
                final String[] cols = line.split(AbstractDatabaseStructure.SEPARATOR);
                final FileItem file = Database.getInstance().getFile(Integer.parseInt(cols[0]));

                file.setKey((SecretKey) Utils.deserializeKeyHex(cols[1]));

                result = file;
            } catch (final Exception e) {
                Logger.getLogger(UserAccessTableReader.class).warn("Kann die Zeile " + line + " nicht lesen!", e);
            }

            return result;
        }
    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableWriter} zum Schreiben einer UserAccess-Table.
     * 
     * @author Smolli
     */
    static final class UserAccessTableWriter extends AbstractDatabaseTableWriter<String> {

        /**
         * Ctor.
         * 
         * @param writer
         *            Der Ziel-Writer.
         * @param items
         *            Die Elemente.
         * @throws DatabaseException
         *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
         */
        public UserAccessTableWriter(final BufferedWriter writer, final List<String> items) throws DatabaseException {
            super(writer, items);
        }

        @Override
        protected String process(final String item) {
            return item;
        }

    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableReader} zum Lesen einer UserFiles-Tabelle.
     * 
     * @author Smolli
     */
    static final class UserFilesTableReader extends AbstractDatabaseTableReader<FileItem> {

        /**
         * Ctor.
         * 
         * @param reader
         *            Quell-Reader.
         * @param user
         *            Der Besitzer der Dateien.
         * @throws DatabaseException
         *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
         */
        public UserFilesTableReader(final BufferedReader reader, final User user) throws DatabaseException {
            super(reader);

            for (final FileItem item : this.getResult()) {
                item.setOwner(user);
            }
        }

        @Override
        protected FileItem process(final String line) {
            FileItem result = null;

            try {
                final String[] cols = line.split(";");
                final int fileId = Integer.parseInt(cols[0]);
                final FileItem file = Database.getInstance().getFile(fileId);

                file.setKey((SecretKey) Utils.deserializeKeyHex(cols[1]));

                result = file;
            } catch (final Exception e) {
                Logger.getLogger(UserFilesTableReader.class).warn("Kann die Zeile " + line + " nicht parsen!", e);
            }

            return result;
        }

    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableWriter} zum Schreiben der UserFiles-Tabelle.
     * 
     * @author Smolli
     */
    static final class UserFilesTableWriter extends AbstractDatabaseTableWriter<FileItem> {

        /**
         * Ctor.
         * 
         * @param writer
         *            Der Ziel-Writer.
         * @param items
         *            Die Elemente.
         * @throws DatabaseException
         *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
         */
        public UserFilesTableWriter(final BufferedWriter writer, final List<FileItem> items) throws DatabaseException {
            super(writer, items);
        }

        @Override
        protected String process(final FileItem item) {
            String result = null;

            try {
                result = Integer.toString(item.getId()) + AbstractDatabaseStructure.SEPARATOR
                        + Utils.serializeKeyHex(item.getKey());
            } catch (final Exception e) {
                Logger.getLogger(UserFilesTableWriter.class)
                        .warn("Kann das Element " + item + " nicht kompilieren!", e);
            }

            return result;
        }

    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableReader} zum Lesen einer UserLend-Tabelle.
     * 
     * @author Smolli
     */
    static final class UserLendTableReader extends AbstractDatabaseTableReader<UserFilePair> {

        /**
         * Ctor.
         * 
         * @param reader
         *            Quell-Reader.
         * @throws DatabaseException
         *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
         */
        public UserLendTableReader(final BufferedReader reader) throws DatabaseException {
            super(reader);
        }

        @Override
        protected UserFilePair process(final String line) {
            UserFilePair result = null;

            try {
                result = UserFilePair.parse(line);
            } catch (final DatabaseException e) {
                Logger.getLogger(UserLendTableReader.class).warn("Kann Zeile " + line + " nicht parsen!", e);
            }

            return result;
        }
    }

    /**
     * Spezialisierter {@link AbstractDatabaseTableReader} zum Lesen einer Users-Tabelle.
     * 
     * @author Smolli
     */
    static final class UsersTableReader extends AbstractDatabaseTableReader<User> {

        /**
         * Ctor.
         * 
         * @param reader
         *            Quell-Reader.
         * @throws DatabaseException
         *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
         */
        public UsersTableReader(final BufferedReader reader) throws DatabaseException {
            super(reader);
        }

        @Override
        protected User process(final String line) {
            User result = null;

            try {
                result = User.parse(line);
            } catch (final Exception e) {
                Logger.getLogger(UsersTableReader.class).warn("Kann Zeile " + line + " nicht parsen!", e);
            }

            return result;
        }
    }

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(DatabaseIO.class);

    /** Standard-Datei für die User-Tabelle. */
    protected static final String DB_USERS_TB = "db/users.tb";

    /** Standard-Datei für die Datei-Tabelle. */
    protected static final String DB_FILES_TB = "db/files.tb";

    /** Das Reentrantlock. */
    protected static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * Prüft die Basisstruktur der Datenbank und erstellt sie bei Bedarf.
     * 
     * @throws IOException
     *             Wird geworfen, wenn die Struktur nicht erstellt werden konnte.
     */
    protected void baseStructureTest() throws IOException {
        this.basePathsTest();

        this.baseFilesTest();
    }

    /**
     * Erstellt die Tabellen-Dateien für die Datenbank.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @throws IOException
     *             Wird geworfen, wenn eine der Dateien nicht erstellt werden konnte.
     */
    protected void createUserFiles(final User user) throws IOException {
        if (!new File(DatabaseTables.FileTable.getFilename(user)).createNewFile()) {
            DatabaseIO.LOG.info("Dateien-Tabelle des Benutzers existiert schon!");
        }

        if (!new File(DatabaseTables.AccessTable.getFilename(user)).createNewFile()) {
            DatabaseIO.LOG.info("Zugriffs-Tabelle des Benutzers existiert schon!");
        }

        if (!new File(DatabaseTables.LendTable.getFilename(user)).createNewFile()) {
            DatabaseIO.LOG.info("Leih-Tabelle des Benutzers existiert schon!");
        }
    }

    /**
     * Löscht die Datenbankdatei physikalisch.
     * 
     * @param item
     *            Das {@link FileItem}, das gelöscht werden soll.
     */
    protected void removeFileFromDisk(final FileItem item) {
        final File file = new File(item.getDatabaseName());

        if (!file.delete()) {
            DatabaseIO.LOG.warn("Kann die Datenbankdatei nicht löschen!");
        }
    }

    /**
     * Testet, ob die Basistabellen vorhanden sind und erstellt sie bei Bedarf.
     * 
     * @throws IOException
     *             Wird geworfen, wenn die Basistabellen nicht erstellt werden können.
     */
    private void baseFilesTest() throws IOException {
        if (new File(DatabaseIO.DB_USERS_TB).createNewFile()) {
            DatabaseIO.LOG.info("User-Tabelle angelegt.");
        }

        if (new File(DatabaseIO.DB_FILES_TB).createNewFile()) {
            DatabaseIO.LOG.info("Dateien-Tabelle anegelegt.");
        }
    }

    /**
     * Testet, ob die Basispfade vorhanden sind und erstellt sie bei Bedarf.
     */
    private void basePathsTest() {
        this.pathTest("db");

        this.pathTest("db/users");

        this.pathTest("db/files");
    }

    /**
     * Testet, ob ein einzelnes Verzeichnis existiert und legt es bei Bedarf an.
     * 
     * @param path
     *            Das Verzeichnis.
     */
    private void pathTest(final String path) {
        final File test = new File(path);

        if (!(test.exists() && test.isDirectory())) {
            final boolean result = test.mkdir();

            if (result) {
                DatabaseIO.LOG.info("Verzeichnis '" + path + "' erstellt.");
            }
        }
    }
}

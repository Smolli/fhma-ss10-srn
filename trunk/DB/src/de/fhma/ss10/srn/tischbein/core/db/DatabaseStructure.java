package de.fhma.ss10.srn.tischbein.core.db;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.crypto.AESWriter;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseModel.Tables;

public class DatabaseStructure {

    /** Das Reentrantlock. */
    protected static final ReentrantLock LOCK = new ReentrantLock();
    /** Standard-Datei für die User-Tabelle. */
    protected static final String DB_USERS_TB = "db/users.tb";
    /** Standard-Datei für die Datei-Tabelle. */
    protected static final String DB_FILES_TB = "db/files.tb";

    /**
     * Erstellt den verschlüsselten Dateiinhalt.
     * 
     * @param filename
     *            Der Dateiname.
     * @param secret
     *            Der Schlüssel, mit dem der Inhalt verschlüsselt werden soll.
     * @return Das erzugte {@link FileItem} mit den Dateidaten.
     * @throws IOException
     *             Wird geworfen, wenn die Datei nicht gelesen werden konnte.
     */
    protected static FileItem createEncryptedFile(final User owner, final String filename, final byte[] secret)
            throws IOException {
        FileItem fi = FileItem.create(owner, filename, secret);

        AESWriter w = AESWriter.createWriter("db/files/" + Utils.toMD5Hex(fi.getName()), secret);
        w.write(Utils.toHexString(fi.getBuffer()));
        w.close();

        return fi;
    }

    protected DatabaseStructure() {
        super();
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
    protected void createUserFiles(final User user) throws IOException, DatabaseException {
        if (!new java.io.File(Tables.FileTable.getFilename(user)).createNewFile()) {
            System.out.println("Dateien-Tabelle des Benutzers existiert schon!");
        }

        if (!new java.io.File(Tables.AccessTable.getFilename(user)).createNewFile()) {
            System.out.println("Zugriffs-Tabelle des Benutzers existiert schon!");
        }

        if (!new java.io.File(Tables.LendTable.getFilename(user)).createNewFile()) {
            System.out.println("Leih-Tabelle des Benutzers existiert schon!");
        }
    }

}

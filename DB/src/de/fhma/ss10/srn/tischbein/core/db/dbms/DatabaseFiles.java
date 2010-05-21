package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.IOException;

import de.fhma.ss10.srn.tischbein.core.db.User;

/**
 * Basisklasse der Datenbank. Hier geht es um das Dateisystem.
 * 
 * @author Smolli
 */
public abstract class DatabaseFiles {

    /** Standard-Datei für die User-Tabelle. */
    protected static final String DB_USERS_TB = "db/users.tb";
    /** Standard-Datei für die Datei-Tabelle. */
    protected static final String DB_FILES_TB = "db/files.tb";

    /**
     * Erstellt die Tabellen-Dateien für die Datenbank.
     * 
     * @param user
     *            Der Benutzerkontext.
     * @throws IOException
     *             Wird geworfen, wenn eine der Dateien nicht erstellt werden konnte.
     */
    protected void createUserFiles(final User user) throws IOException {
        if (!new java.io.File(DatabaseTables.FileTable.getFilename(user)).createNewFile()) {
            System.out.println("Dateien-Tabelle des Benutzers existiert schon!");
        }

        if (!new java.io.File(DatabaseTables.AccessTable.getFilename(user)).createNewFile()) {
            System.out.println("Zugriffs-Tabelle des Benutzers existiert schon!");
        }

        if (!new java.io.File(DatabaseTables.LendTable.getFilename(user)).createNewFile()) {
            System.out.println("Leih-Tabelle des Benutzers existiert schon!");
        }
    }

    /**
     * Testet, ob die Basistabellen vorhanden sind und erstellt sie bei Bedarf.
     * 
     * @throws IOException
     *             Wird geworfen, wenn die Basistabellen nicht erstellt werden können.
     */
    protected void testBaseFiles() throws IOException {
        if (new java.io.File(DatabaseFiles.DB_USERS_TB).createNewFile()) {
            System.out.println("User-Tabelle angelegt.");
        }

        if (new java.io.File(DatabaseFiles.DB_FILES_TB).createNewFile()) {
            System.out.println("Dateien-Tabelle anegelegt.");
        }
    }

}

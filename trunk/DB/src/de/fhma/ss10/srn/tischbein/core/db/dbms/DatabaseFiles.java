package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.File;
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
        if (!new File(DatabaseTables.FileTable.getFilename(user)).createNewFile()) {
            System.out.println("Dateien-Tabelle des Benutzers existiert schon!");
        }

        if (!new File(DatabaseTables.AccessTable.getFilename(user)).createNewFile()) {
            System.out.println("Zugriffs-Tabelle des Benutzers existiert schon!");
        }

        if (!new File(DatabaseTables.LendTable.getFilename(user)).createNewFile()) {
            System.out.println("Leih-Tabelle des Benutzers existiert schon!");
        }
    }

    /**
     * Prüft die Basisstruktur der Datenbank und erstellt sie bei Bedarf.
     * 
     * @throws IOException
     *             Wird geworfen, wenn die Struktur nicht erstellt werden konnte.
     */
    protected void testBaseStructure() throws IOException {
        this.testBasePaths();

        this.testBaseFiles();
    }

    /**
     * Testet, ob die Basistabellen vorhanden sind und erstellt sie bei Bedarf.
     * 
     * @throws IOException
     *             Wird geworfen, wenn die Basistabellen nicht erstellt werden können.
     */
    private void testBaseFiles() throws IOException {
        if (new File(DatabaseFiles.DB_USERS_TB).createNewFile()) {
            System.out.println("User-Tabelle angelegt.");
        }

        if (new File(DatabaseFiles.DB_FILES_TB).createNewFile()) {
            System.out.println("Dateien-Tabelle anegelegt.");
        }
    }

    /**
     * Testet, ob die Basispfade vorhanden sind und erstellt sie bei Bedarf.
     */
    private void testBasePaths() {
        this.testDir("db");

        this.testDir("db/users");

        this.testDir("db/files");
    }

    /**
     * Testet, ob ein einzelnes Verzeichnis existiert und legt es bei Bedarf an.
     * 
     * @param path
     *            Das Verzeichnis.
     */
    private void testDir(final String path) {
        File test = new File(path);

        if (!(test.exists() && test.isDirectory())) {
            if (test.mkdir()) {
                System.out.println("Verzeichnis '" + path + "' erstellt.");
            }
        }
    }
}

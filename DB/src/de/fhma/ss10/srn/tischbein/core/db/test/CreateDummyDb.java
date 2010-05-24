package de.fhma.ss10.srn.tischbein.core.db.test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;

/**
 * Erstellt eine Dummy-Datenbank. Kann zum Testen verwendet werden.
 * 
 * @author Smolli
 */
public class CreateDummyDb {

    /**
     * Erstellt eine Testdatenbank.
     * 
     * @param args
     *            Wird nicht verwendet.
     */
    public static void main(final String[] args) {
        try {
            CreateDummyDb.createUsers();

            User user = Database.getInstance().getUser("Susi");

            user.unlock("1234");

            FileItem file = user.addFile("Testdatei.txt");
            user.addFile("Noch eine Datei.txt");
            user.addFile("letzte Datei.txt");

            user.getFileListObject().setAccess(Database.getInstance().getUser("franz"), file, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Erstellt die Protagonisten, wenn sie noch nicht existieren.
     * 
     * @throws DatabaseException
     *             Wird geworfen, wenn die Benutzer nicht erstellt werden konnten.
     */
    private static void createUsers() throws DatabaseException {
        if (!Database.getInstance().hasUser("susi")) {
            Database.getInstance().createUser("Susi", "1234");
        }

        if (!Database.getInstance().hasUser("franz")) {
            Database.getInstance().createUser("Franz", "1234");
        }
    }

}

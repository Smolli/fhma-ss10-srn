package de.fhma.ss10.srn.tischbein.core.db.test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
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
            Database db = Database.getInstance();

            if (!db.hasUser("Susi")) {
                db.createUser("Susi", "1234");
            }

            User user = db.getUser("Susi");

            user.unlock("1234");

            user.addFile("Testdatei.txt");
            user.addFile("Noch eine Datei.txt");
            user.addFile("letzte Datei.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

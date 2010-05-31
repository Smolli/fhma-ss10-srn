package de.fhma.ss10.srn.tischbein.core.db.test;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;

/**
 * Erstellt eine Dummy-Datenbank. Kann zum Testen verwendet werden.
 * 
 * @author Smolli
 */
public final class CreateDummyDb {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(CreateDummyDb.class);

    /**
     * Erstellt eine Testdatenbank.
     * 
     * @param args
     *            Wird nicht verwendet.
     */
    public static void main(final String[] args) {
        try {
            CreateDummyDb.createUsers();

            final User user = Database.getInstance().getUser("Susi");

            user.unlock("1234");

            // TODO: auf neuen Aufruf umstellen
            //            FileItem file = user.addFile("Testdatei.txt");
            //            user.addFile("Big File.txt");
            //            user.addFile("letzte Datei.txt");

            //            user.getDescriptor().setAccess(Database.getInstance().getUser("franz"), file, true);
        } catch (final Exception e) {
            CreateDummyDb.LOG.error("Kann die Datenbank nicht erzeugen!", e);
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

    /**
     * Geschützter Ctor.
     */
    private CreateDummyDb() {
        super();
    }

}

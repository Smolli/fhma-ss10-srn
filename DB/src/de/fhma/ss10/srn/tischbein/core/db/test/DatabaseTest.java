package de.fhma.ss10.srn.tischbein.core.db.test;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;

/**
 * Testet die Konsistenz der Datenbank.
 * 
 * @author Smolli
 */
public class DatabaseTest {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(DatabaseTest.class);
    /** Testdatei. */
    private static final String TESTFILE = "Testfile.txt";
    /** Testpasswort. */
    private static final String TEST_SECRET = "secret!";
    /** Testuser. */
    private static final String TEST_USER = "UserThatWillBeDeleted";

    /** Hält den Testbenutzer. */
    private User user;

    /**
     * Die Datenbank wird hier vor der erstmaligen Verwendung initialisiert.
     */
    public void setupBeforeClass() {
        final File file = new File(DatabaseTest.TESTFILE);

        if (!file.exists()) {
            try {
                final FileOutputStream fos = new FileOutputStream(file);

                fos.write("Testtext. Sehr Dingsig!".getBytes());

                fos.close();
            } catch (final Exception e) {
                DatabaseTest.LOG.fatal("Setup error!", e);

                Assert.fail("Setup error!");
            }
        }
    }

    /**
     * Testet das Singleton-Verhalten.
     */
    @Test
    public void test00Singleton() {
        Assert.assertNotNull(Database.getInstance());
    }

    /**
     * Prüft ob der Benutzer nicht existiert.
     */
    @Test
    public void test01HasUserNegative() {
        Assert.assertFalse(Database.getInstance().hasUser(DatabaseTest.TEST_USER));
    }

    /**
     * Prüft das Erstellen des Benutzers.
     */
    @Test
    public void test02CreateUser() {
        try {
            Database.getInstance().createUser(DatabaseTest.TEST_USER, DatabaseTest.TEST_SECRET);
        } catch (final DatabaseException e) {
            DatabaseTest.LOG.fatal("Kann keinen Benutzer anlegen!", e);

            Assert.fail();
        }
    }

    /**
     * Prüft, ob die Existenz eines Benutzers abgefragt werden kann.
     */
    @Test
    public void test03HasUser() {
        Assert.assertTrue(Database.getInstance().hasUser(DatabaseTest.TEST_USER));
    }

    /**
     * Ermittelt einen Benutzer.
     */
    @Test
    public void test04GetUser() {
        try {
            Assert.assertNotNull(Database.getInstance().getUser(DatabaseTest.TEST_USER));
        } catch (final DatabaseException e) {
            DatabaseTest.LOG.fatal("Datenbank gibt keinen Benutzer zurück!", e);

            Assert.fail();
        }
    }

    /**
     * Prüft das Login eines Users.
     */
    @Test
    public void test05LoginUser() {
        try {
            if (this.user != null) {
                Assert.fail("Setup error!");
            }

            this.user = Database.getInstance().getUser(DatabaseTest.TEST_USER);

            this.user.unlock(DatabaseTest.TEST_SECRET);

            Assert.assertNotNull(this.user);
        } catch (final Exception e) {
            DatabaseTest.LOG.fatal("Kann den Benutzer nicht anmelden!", e);

            Assert.fail();
        }
    }

    /**
     * Fügt eine Datei zum Benutzer hinzu.
     */
    @Test
    public void test06AddFile() {
        // TODO: auf neuen Aufruf umstellen
        //        try {
        //                        this.user.addFile(DatabaseTest.TESTFILE);
        //        } catch (DatabaseException e) {
        //            e.printStackTrace();
        //
        Assert.fail();
        //        }
    }

    /**
     * Testet den Logout eines Benutzers.
     */
    @Test
    public void test07LogoutUser() {
        this.user.lock();

        Assert.assertTrue(true);
    }

    /**
     * Prüft, ob ein Benutzers auch wieder gelöscht werden kann.
     */
    @Test
    public void test08RemoveUser() {
        //        Database.getInstance().removeUser(TEST_USER);
        Assert.fail();
    }

}

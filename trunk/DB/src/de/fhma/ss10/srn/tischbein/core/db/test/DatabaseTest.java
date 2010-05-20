package de.fhma.ss10.srn.tischbein.core.db.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;

public class DatabaseTest {

    private static final String TESTFILE = "Testfile.txt";

    public void setupBeforeClass() {
        File file = new File(TESTFILE);

        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);

                fos.write("Testtext. Sehr Dingsig!".getBytes());

                fos.close();
            } catch (Exception e) {
                e.printStackTrace();

                fail("Setup error!");
            }
        }

        user = null;
    }

    private static final String TEST_SECRET = "secret!";
    private static final String TEST_USER = "UserThatWillBeDeleted";
    private User user;

    @Test
    public void testSingleton() {
        assertNotNull(Database.getInstance());
    }

    @Test
    public void testHasUserNegative() {
        assertFalse(Database.getInstance().hasUser(TEST_USER));
    }

    @Test
    public void testCreateUser() {
        try {
            Database.getInstance().createUser(TEST_USER, TEST_SECRET);
        } catch (DatabaseException e) {
            e.printStackTrace();

            fail();
        }
    }

    @Test
    public void testHasUser() {
        assertTrue(Database.getInstance().hasUser(TEST_USER));
    }

    @Test
    public void testGetUser() {
        try {
            assertNotNull(Database.getInstance().getUser(TEST_USER));
        } catch (DatabaseException e) {
            e.printStackTrace();

            fail();
        }
    }

    @Test
    public void testLoginUser() {
        try {
            if (user != null)
                fail("Setup error!");

            user = Database.getInstance().getUser(TEST_USER);

            user.unlock(TEST_SECRET);

            assertNotNull(user);
        } catch (Exception e) {
            e.printStackTrace();

            fail();
        }
    }

    @Test
    public void testAddFile() {
        try {
            user.addFile(TESTFILE);
        } catch (DatabaseException e) {
            e.printStackTrace();

            fail();
        }
    }

    @Test
    public void testLogoutUser() {
        user.lock();
    }

    @Test
    public void testRemoveUser() {
        //        Database.getInstance().removeUser(TEST_USER);
    }

}

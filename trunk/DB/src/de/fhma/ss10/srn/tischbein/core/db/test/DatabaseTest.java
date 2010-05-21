package de.fhma.ss10.srn.tischbein.core.db.test;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;
import de.fhma.ss10.srn.tischbein.core.db.User;

public class DatabaseTest {

    private static final String TESTFILE = "Testfile.txt";

    private static final String TEST_SECRET = "secret!";

    private static final String TEST_USER = "UserThatWillBeDeleted";
    private User user;

    public void setupBeforeClass() {
        File file = new File(DatabaseTest.TESTFILE);

        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);

                fos.write("Testtext. Sehr Dingsig!".getBytes());

                fos.close();
            } catch (Exception e) {
                e.printStackTrace();

                Assert.fail("Setup error!");
            }
        }

        this.user = null;
    }

    @Test
    public void test00Singleton() {
        Assert.assertNotNull(Database.getInstance());
    }

    @Test
    public void test01HasUserNegative() {
        Assert.assertFalse(Database.getInstance().hasUser(DatabaseTest.TEST_USER));
    }

    @Test
    public void test02CreateUser() {
        try {
            Database.getInstance().createUser(DatabaseTest.TEST_USER, DatabaseTest.TEST_SECRET);
        } catch (DatabaseException e) {
            e.printStackTrace();

            Assert.fail();
        }
    }

    @Test
    public void test03HasUser() {
        Assert.assertTrue(Database.getInstance().hasUser(DatabaseTest.TEST_USER));
    }

    @Test
    public void test04GetUser() {
        try {
            Assert.assertNotNull(Database.getInstance().getUser(DatabaseTest.TEST_USER));
        } catch (DatabaseException e) {
            e.printStackTrace();

            Assert.fail();
        }
    }

    @Test
    public void test05LoginUser() {
        try {
            if (this.user != null) {
                Assert.fail("Setup error!");
            }

            this.user = Database.getInstance().getUser(DatabaseTest.TEST_USER);

            this.user.unlock(DatabaseTest.TEST_SECRET);

            Assert.assertNotNull(this.user);
        } catch (Exception e) {
            e.printStackTrace();

            Assert.fail();
        }
    }

    @Test
    public void test06AddFile() {
        try {
            this.user.addFile(DatabaseTest.TESTFILE);
        } catch (DatabaseException e) {
            e.printStackTrace();

            Assert.fail();
        }
    }

    @Test
    public void test07LogoutUser() {
        this.user.lock();
    }

    @Test
    public void test08RemoveUser() {
        //        Database.getInstance().removeUser(TEST_USER);
    }

}

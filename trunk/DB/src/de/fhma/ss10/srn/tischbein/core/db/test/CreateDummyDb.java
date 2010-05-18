package de.fhma.ss10.srn.tischbein.core.db.test;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.User;

public class CreateDummyDb {

    public static void main(String[] args) {
        try {
            Database db = Database.getInstance();

            db.createUser("Susi", "1234");

            User user = db.loginUser("Susi", "1234");

            db.addFile(user, "Testdatei.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package de.fama.ss10.srn.project.db;

import java.io.File;

/**
 * Datanbankklasse. Kapselt die gesamte Datenbankstruktur.
 * 
 * @author Smolli
 */
public final class Database {

    private String rootPath;

    private Database() {
    }

    public static Database open(final String root) {
        Database db = new Database();
        File path = new File(root);

        if (!path.isDirectory()) {
            throw new IllegalArgumentException("Root path argument must be a directory!");
        }

        db.setRootDir(root);

        return db;
    }

    private void setRootDir(final String root) {
        this.rootPath = root;
    }

}

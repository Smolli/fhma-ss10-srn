package de.fhma.ss10.srn.tischbein.core.db;

import java.util.List;

/**
 * Enthält alle Tupel, die beschreiben, welche Datei an welchen Benutzer freigeschaltet wurde.
 * 
 * @author Smolli
 */
public final class FileListObject {

    /**
     * Tupelklasse zum Halten der User-Datei-Relation.
     * 
     * @author Smolli
     */
    public static final class UserFilePair {

        /** Hält das Benutzerobjekt. */
        private final User user;
        /** Hält das Dateiobjekt. */
        private final File file;

        /**
         * Standard-Ctor, der das Tupel initialisiert.
         * 
         * @param userObject
         *            Das Benutzerobjekt.
         * @param fileObject
         *            Das Dateiobjekt.
         */
        public UserFilePair(final User userObject, final File fileObject) {
            this.user = userObject;
            this.file = fileObject;
        }

        /**
         * Gibt das Dateiobjekt zurück.
         * 
         * @return Das {@link File}-Objekt.
         */
        public File getFile() {
            return this.file;
        }

        /**
         * Gibt das Benutzerobjekt zurück.
         * 
         * @return Das {@link User}-Objekt.
         */
        public User getUser() {
            return this.user;
        }

    }

    /**
     * Setzt die Liste mit den Dateien anderer Benutzer, auf die der Benutzer zugreifen darf.
     * 
     * @param accessTable
     *            Die Liste der Dateien.
     */
    void setAccessTable(final List<File> accessTable) {
        // TODO Auto-generated method stub

    }

    /**
     * Setzt die Liste der Dateien, die dem Bentuzer gehören.
     * 
     * @param filesTable
     *            Die List der Dateien.
     */
    void setFilesTable(final List<File> filesTable) {
        // TODO Auto-generated method stub

    }

    /**
     * Setzt die Liste der Tupel, die angeben, welche Datei für welchen anderen Benutzer freigegeben wurde.
     * 
     * @param list
     *            Die Liste.
     */
    void setLendTable(final List<UserFilePair> list) {
        // TODO Auto-generated method stub

    }

}

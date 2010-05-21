package de.fhma.ss10.srn.tischbein.core.db;

import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseStructure;

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

        /**
         * Parst du übergebene Zeile und gibt sie als {@link UserFilePair} zurück.
         * 
         * @param line
         *            Die Tabellenzeile.
         * @return Das {@link UserFilePair}-Objekt.
         * @throws DatabaseException
         *             Wird geworfen, wenn die Zeile nicht geparst werden konnte.
         */
        public static UserFilePair parse(final String line) throws DatabaseException {
            String[] cols = line.split(";");

            String userName = cols[0];
            int fileId = Integer.parseInt(cols[1]);

            FileItem fileObject = Database.getInstance().getFile(fileId);
            User userObject = Database.getInstance().getUser(userName);

            return new UserFilePair(userObject, fileObject);
        }

        /** Hält das Benutzerobjekt. */
        private final User user;

        /** Hält das Dateiobjekt. */
        private final FileItem file;

        /**
         * Standard-Ctor, der das Tupel initialisiert.
         * 
         * @param userObject
         *            Das Benutzerobjekt.
         * @param fileObject
         *            Das Dateiobjekt.
         */
        public UserFilePair(final User userObject, final FileItem fileObject) {
            this.user = userObject;
            this.file = fileObject;
        }

        /**
         * Erstellt aus dem aktuellen Objekt eine Tabellenzeile.
         * 
         * @return Die Tabellenzeile.
         */
        public String compile() {
            return this.user.getName() + DatabaseStructure.SEPARATOR + this.file.getId();
        }

        /**
         * Gibt das Dateiobjekt zurück.
         * 
         * @return Das {@link FileItem}-Objekt.
         */
        public FileItem getFile() {
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

    /** Hält die Tabelle der Dateien anderer Benutzer, auf die der User zugriff hat. */
    private Vector<FileItem> accessList;
    /** Hält die Tabelle der Dateien, die dem Benutzer gehören. */
    private Vector<FileItem> filesList;
    /** Hält die Tabelle der Benutzer, denen die Dateien ausgeliehen wurden. */
    private Vector<UserFilePair> lendList;

    /**
     * Gibt die Zugriffstabelle zurück.
     * 
     * @return Die Tabelle der Dateien, auf die der Benutzer zugriff hat.
     */
    public Vector<FileItem> getAccessList() {
        return this.accessList;
    }

    /**
     * Gibt die Dateien-Tabelle zurück.
     * 
     * @return Die Dateien-Tabelle als {@link Vector}.
     */
    public Vector<FileItem> getFileList() {
        return this.filesList;
    }

    /**
     * Die Relation der Dateien und Benutzer, die auf die Dateien des Benutzers zugreifen können.
     * 
     * @return Die Relation als {@link Vector}.
     */
    public Vector<UserFilePair> getLendList() {
        return this.lendList;
    }

    /**
     * Gibt an, ob der angegebene Benutzer zugriff auf die angegebene Datei hat.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @return Gibt <code>true</code> zurück, wenn der Benutzer auf die Datei zugreifen darf, andernfalls
     *         <code>false</code>.
     */
    public boolean hasAccess(final User user, final FileItem file) {
        for (UserFilePair ufp : this.getLendList()) {
            if (ufp.getUser().equals(user) && ufp.getFile().equals(file)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Setzt oder entzieht die Berechtigung dem angegebenen Benutzer für die angegebene Datei.
     * 
     * @param user
     *            Der Benutzer.
     * @param file
     *            Die Datei.
     * @param value
     *            Wenn der Wert auf <code>true</code> gesetzt ist, wird die Berechtigung erteilt, andernfalls wird sie
     *            entzogen.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Änderung nicht gespeichert werden konnte.
     */
    public void setAccess(final User user, final FileItem file, final Boolean value) throws DatabaseException {
        if (value) {
            Database.getInstance().grantAccess(user, file);
        } else {
            Database.getInstance().denyAccess(user, file);
        }
    }

    /**
     * Setzt die Liste mit den Dateien anderer Benutzer, auf die der Benutzer zugreifen darf.
     * 
     * @param accessTable
     *            Die Liste der Dateien.
     */
    public void setAccessTable(final Vector<FileItem> accessTable) {
        this.accessList = accessTable;
    }

    /**
     * Setzt die Liste der Dateien, die dem Bentuzer gehören.
     * 
     * @param filesTable
     *            Die List der Dateien.
     */
    public void setFilesTable(final Vector<FileItem> filesTable) {
        this.filesList = filesTable;
    }

    /**
     * Setzt die Liste der Tupel, die angeben, welche Datei für welchen anderen Benutzer freigegeben wurde.
     * 
     * @param list
     *            Die Liste.
     */
    public void setLendTable(final Vector<UserFilePair> list) {
        this.lendList = list;
    }

}

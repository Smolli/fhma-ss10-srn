package de.fhma.ss10.srn.tischbein.core.db.dbms;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.db.User;

/**
 * Alle möglichen Tabellen (Helferklasse).
 * 
 * @author Smolli
 */
public enum DatabaseTables {

    /** Die Tabelle mit den Dateien anderer Benutzer, auf die der Benutzer Zugang hat. */
    AccessTable,
    /** Die Tabelle mit den Tupeln, welche Datei der Benutzer anderen Benutzern zugänglich gemacht hat. */
    LendTable,
    /** Die Tabelle mit den Dateien des Benutzers. */
    FileTable;

    /**
     * Gibt den Dateinamen der Tabelle im Benutzerkontext zurück.
     * 
     * @param user
     *            Der {@link User}-Kontext.
     * @return Der eindeutige Dateiname.
     */
    public String getFilename(final User user) {
        StringBuilder sb = new StringBuilder("db/users/");

        sb.append(Utils.toMD5Hex(user.getName()));

        if (this == DatabaseTables.FileTable) {
            sb.append(".files");
        }

        if (this == AccessTable) {
            sb.append(".access");
        }

        if (this == LendTable) {
            sb.append(".lend");
        }

        sb.append(".tb");

        return sb.toString();
    }

}

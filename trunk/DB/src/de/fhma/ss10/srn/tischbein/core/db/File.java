package de.fhma.ss10.srn.tischbein.core.db;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Fileklasse. Enhält alle Methoden zur Verwaltung der verschlüsselten Dateien.
 * 
 * @author Smolli
 */
public final class File {

    /** Datei-Tabelle Schlüssel. */
    private static final int COLUMN_KEY = 3;
    /** Datei-Tabelle Hash. */
    private static final int COLUMN_HASH = 2;
    /** Datei-Tabelle Name. */
    private static final int COLUMN_NAME = 1;
    /** Datei-Tabelle ID. */
    private static final int COLUMN_ID = 0;

    /**
     * Parst die angegebene Zeile und gibt sie als {@link File}-Objekt zurück.
     * 
     * @param line
     *            Die zu parsende Zeile.
     * @return Das {@link File}-Objekt.
     */
    static File parse(final String line) {
        String[] cols = line.split(";");
        File file = new File();

        file.setId(Integer.parseInt(cols[File.COLUMN_ID]));
        file.setName(cols[File.COLUMN_NAME]);
        file.setHash(cols[File.COLUMN_HASH]);
        file.setKey(Utils.fromHexString(cols[File.COLUMN_KEY]));

        return file;
    }

    /** Hält die ID der Datei. */
    private int id;
    /** Hält den Dateinamen, wie er angezeigt werden soll. */
    private String fileName;
    /** Hält die MD5-Summe des Dateiinhalts. */
    private String hash;
    /** Hält den Dateischlüssel. */
    private byte[] fileKey;

    /**
     * Versteckter Standard-Ctor.
     */
    private File() {
    }

    /**
     * Gibt die ID der Datei zurück.
     * 
     * @return Die ID.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setzt die Hash-Summe des Dateiinhalts. Dient zur späteren Überprüfung, ob die Datei erfolgreich entschlüsselt
     * wurde.
     * 
     * @param value
     *            Die MD5-Summe des Dateiinhalts.
     */
    private void setHash(final String value) {
        this.hash = value;
    }

    /**
     * Setzt die ID der Datei.
     * 
     * @param value
     *            Die ID.
     */
    private void setId(final int value) {
        this.id = value;
    }

    /**
     * Setzt den Schlüssel, mit dem der Inhalt der Datei verschlüssel ist.
     * 
     * @param value
     *            Der Schlüssel.
     */
    private void setKey(final byte[] value) {
        this.fileKey = value;
    }

    /**
     * Setzt den Namen der Datei, wie er in der Oberfläche angezeigt werden soll.
     * 
     * @param value
     *            Der Dateiname.
     */
    private void setName(final String value) {
        this.fileName = value;
    }

}

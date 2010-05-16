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

    private File() {
    }

    public int getId() {
        return this.id;
    }

    private void setHash(final String value) {
        this.hash = value;
    }

    private void setId(final int value) {
        this.id = value;
    }

    private void setKey(final byte[] value) {
        this.fileKey = value;
    }

    private void setName(final String value) {
        this.fileName = value;
    }

}

package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import de.fhma.ss10.srn.tischbein.core.Utils;

/**
 * Fileklasse. Enhält alle Methoden zur Verwaltung der verschlüsselten Dateien.
 * 
 * @author Smolli
 */
public final class FileItem {

    /** Datei-Tabelle Hash. */
    private static final int COLUMN_HASH = 2;
    /** Datei-Tabelle Name. */
    private static final int COLUMN_NAME = 1;
    /** Datei-Tabelle ID. */
    private static final int COLUMN_ID = 0;

    /**
     * Parst die angegebene Zeile und gibt sie als {@link FileItem}-Objekt zurück.
     * 
     * @param line
     *            Die zu parsende Zeile.
     * @return Das {@link FileItem}-Objekt.
     */
    static FileItem parse(final String line) {
        String[] cols = line.split(";");
        FileItem file = new FileItem();

        file.setId(Integer.parseInt(cols[FileItem.COLUMN_ID]));
        file.setName(cols[FileItem.COLUMN_NAME]);
        file.setHash(Utils.fromHexString(cols[FileItem.COLUMN_HASH]));

        return file;
    }

    /** Hält die ID der Datei. */
    private int id;
    /** Hält den Dateinamen, wie er angezeigt werden soll. */
    private String fileName;
    /** Hält die MD5-Summe des Dateiinhalts. */
    private byte[] hash;
    /** Hält den Dateischlüssel. */
    private byte[] fileKey;
    private byte[] buffer;

    /**
     * Versteckter Standard-Ctor.
     */
    private FileItem() {
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
     * Setzt den Schlüssel, mit dem der Inhalt der Datei verschlüssel ist.
     * 
     * @param value
     *            Der Schlüssel.
     */
    void setKey(final byte[] value) {
        this.fileKey = value;
    }

    /**
     * Setzt die Hash-Summe des Dateiinhalts. Dient zur späteren Überprüfung, ob die Datei erfolgreich entschlüsselt
     * wurde.
     * 
     * @param bs
     *            Die MD5-Summe des Dateiinhalts.
     */
    private void setHash(final byte[] bs) {
        this.hash = bs;
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
     * Setzt den Namen der Datei, wie er in der Oberfläche angezeigt werden soll.
     * 
     * @param value
     *            Der Dateiname.
     */
    private void setName(final String value) {
        this.fileName = value;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getName() {
        return this.fileName;
    }

    public static FileItem create(String filename, byte[] secret) throws IOException {
        FileItem fi = new FileItem();
        File file = new File(filename);

        fi.buffer = new byte[(int) file.length()];
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        bis.read(fi.buffer);

        bis.close();

        fi.setHash(Utils.toMD5(fi.buffer));
        fi.setName(file.getName());
        fi.setId(Database.getInstance().getNextFileId());
        fi.setKey(secret);

        return fi;
    }

    public String compile() {
        return MessageFormat.format("{1}{0}{2}{0}{3}\n", Database.SEPARATOR, Integer.toString(this.id), this.getName(),
                this.hash);
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public byte[] getKey() {
        return this.fileKey;
    }

}

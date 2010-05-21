package de.fhma.ss10.srn.tischbein.core.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;

import de.fhma.ss10.srn.tischbein.core.Utils;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseStructure;

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
     * Erstellt ein neues {@link FileItem} anhand des übergebenen Dateinamens erstellt.
     * 
     * @param owner
     *            Der Besitzer der Datei.
     * @param filename
     *            Der Dateiname der zugrundeliegenden Datei.
     * @param secret
     *            Der Schlüssel, mit dem der Inhalt der Datei verschlüsselt werden soll.
     * @return Gibt das erstellte {@link FileItem} zurück.
     * @throws IOException
     *             Wird geworfen, wenn die Datei nicht gefunden oder gelesen werden konnte.
     */
    public static FileItem create(final User owner, final String filename, final byte[] secret) throws IOException {
        FileItem fi = new FileItem(owner);
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

    /**
     * Parst die angegebene Zeile und gibt sie als {@link FileItem}-Objekt zurück.
     * 
     * @param owner
     *            Der Besitzer der Datei.
     * @param line
     *            Die zu parsende Zeile.
     * @return Das {@link FileItem}-Objekt.
     */
    public static FileItem parse(final User owner, final String line) {
        String[] cols = line.split(DatabaseStructure.SEPARATOR);
        FileItem file = new FileItem(owner);

        file.setId(Integer.parseInt(cols[FileItem.COLUMN_ID]));
        file.setName(cols[FileItem.COLUMN_NAME]);
        file.setHash(Utils.fromHexLine(cols[FileItem.COLUMN_HASH]));

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
    /** Hält den unverschlüsselten Dateiinhalt. */
    private byte[] buffer;
    /** Hält den Benistzer der Datei oder <code>null</code> wenn der Besitzer nicht bekannt ist. */
    private User owner;

    /**
     * Versteckter Standard-Ctor.
     * 
     * @param ownerObject
     *            Das {@link User}-Objekt, dass der Besitzer der Datei ist.
     */
    private FileItem(final User ownerObject) {
        this.owner = ownerObject;
    }

    /**
     * Erstellt einen CSV-String für die Datenbank.
     * 
     * @return Die Tabellenzeile.
     */
    public String compile() {
        return MessageFormat.format("{1}{0}{2}{0}{3}\n", DatabaseStructure.SEPARATOR, Integer.toString(this.id), this
                .getName(), Utils.toHexLine(this.hash));
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof FileItem)) {
            return false;
        }

        FileItem item = (FileItem) obj;

        if (this.id == item.id) {
            return true;
        }

        return false;
    }

    /**
     * Gibt den unverschlüsselten Dateiinhalt zurück.
     * 
     * @return Der Dateiinhalt.
     */
    public byte[] getBuffer() {
        return this.buffer;
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
     * Gibt den Schlüssel zurück, mit dem der Dateiinhalt verschlüsselt wurde.
     * 
     * @return Der Schlüssel.
     */
    public byte[] getKey() {
        return this.fileKey;
    }

    /**
     * Gibt den Dateinamen zurück.
     * 
     * @return Der Dateiname, wie er in der Oberfläche erscheinen soll.
     */
    public String getName() {
        return this.fileName;
    }

    /**
     * Gibt den Besitzer der Datei zurück oder <code>null</code> wenn der Besitzer nicht bekannt ist.
     * 
     * @return Der Besitzer.
     */
    public User getOwner() {
        return this.owner;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    /**
     * Setzt den Schlüssel, mit dem der Inhalt der Datei verschlüssel ist.
     * 
     * @param value
     *            Der Schlüssel.
     */
    public void setKey(final byte[] value) {
        this.fileKey = value;
    }

    /**
     * Setzt den Benitzer auf das angegeben {@link User}-Objekt.
     * 
     * @param user
     *            Der Besitzer der Datei.
     */
    public void setOwner(final User user) {
        this.owner = user;
    }

    @Override
    public String toString() {
        return this.getName();
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

}

package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedWriter;
import java.util.Collection;

import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;

/**
 * Helferklasse zum Schreiben von Tabellen.
 * 
 * @author Smolli
 * @param <T>
 *            Jede beliebige Klasse.
 */
abstract class DatabaseTableWriter<T> {

    /**
     * Erstellt einen neuen {@link DatabaseTableWriter} und startet die Verarbeitung.
     * 
     * @param writer
     *            Der Writer, in den geschrieben werden soll.
     * @param items
     *            Die einzelnen Objekte, die verarbeitet werden sollen.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
     */
    public DatabaseTableWriter(final BufferedWriter writer, final Collection<T> items) throws DatabaseException {
        try {
            for (T item : items) {
                writer.write(this.process(item));
                writer.write("\n");
            }

            writer.close();
        } catch (Exception e) {
            throw new DatabaseException("Kann Tabelle nicht schreiben!", e);
        }
    }

    /**
     * Hier werden die Objekte verarbeitet.
     * 
     * @param item
     *            Das Objekt.
     * @return Die resultierende Zeile, die gespeichert werden soll.
     * @throws Exception
     *             Wird geworfen, wenn das Objekt nicht verarbeitet werden konnte.
     */
    protected abstract String process(T item) throws Exception;

}

package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedWriter;
import java.util.List;

import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;

/**
 * Helferklasse zum Schreiben von Tabellen.
 * 
 * @author Smolli
 * @param <T>
 *            Jede beliebige Klasse.
 */
abstract class AbstractDatabaseTableWriter<T> {

    /**
     * Erstellt einen neuen {@link AbstractDatabaseTableWriter} und startet die Verarbeitung.
     * 
     * @param writer
     *            Der Writer, in den geschrieben werden soll.
     * @param items
     *            Die einzelnen Objekte, die verarbeitet werden sollen.
     * @throws DatabaseException
     *             Wird geworfen, wenn die Tabelle nicht geschrieben werden konnte.
     */
    public AbstractDatabaseTableWriter(final BufferedWriter writer, final List<T> items) throws DatabaseException {
        try {
            for (final T item : items) {
                if (item != null) {
                    writer.write(this.process(item));
                    writer.write("\n");
                }
            }

            writer.close();
        } catch (final Exception e) {
            throw new DatabaseException("Kann Tabelle nicht schreiben!", e);
        }
    }

    /**
     * Hier werden die Objekte verarbeitet.
     * 
     * @param item
     *            Das Objekt.
     * @return Die resultierende Zeile, die gespeichert werden soll.
     */
    protected abstract String process(T item);

}

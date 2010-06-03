package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Kleine Hilfsklasse um die Coderedundanz zu verringern. Ließt einen {@link BufferedReader} zeilenweise aus und
 * übergibt jede Zeile einer Callback-Methode zum Verarbeiten. Das Ergebnis wird in einem Vector mit dem Type
 * <code>T</code> gespeichert.
 * 
 * @author Smolli
 * @param <T>
 *            Jede beliebige Klasse.
 */
abstract class AbstractDatabaseTableReader<T> {

    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(AbstractDatabaseTableReader.class);
    /** Hält den Ergebnisvector. */
    private final transient List<T> list = new ArrayList<T>();

    /**
     * Standard-Ctor. Startet auch gleichzeitig die Verarbeitung.
     * 
     * @param reader
     *            Ein {@link BufferedReader}-Objekt, das zeilenweise ausgelesen werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
     */
    public AbstractDatabaseTableReader(final BufferedReader reader) throws DatabaseException {

        try {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) { // NOPMD by smolli on 30.05.10 20:02
                final T item = this.process(line);

                if (item != null) {
                    this.list.add(item);
                }

                count++;
            }

            AbstractDatabaseTableReader.LOG.trace(MessageFormat.format("{0} Zeilen aus Datei {1} gelesen.", Integer
                    .toString(count), reader.toString()));

            reader.close();
        } catch (final Exception e) {
            throw new DatabaseException("Kann die Datei nicht lesen!", e);
        }
    }

    /**
     * Gibt den Ergbenisvektor zurück.
     * 
     * @return Das Ergebnis des Auslesens als {@link List}.
     */
    public List<T> getResult() {
        return this.list;
    }

    /**
     * Abstrakte Callback-Methode. Sie muss das Ergebnis der Zeile als <code>T</code>-Objekt zurück geben.
     * 
     * @param line
     *            Die zu verarbeitende Zeile
     * @return Das Objekt, das im Ergebnisvektor gespeichert werden soll.
     */
    protected abstract T process(final String line);

}

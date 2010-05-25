package de.fhma.ss10.srn.tischbein.core.db.dbms;

import java.io.BufferedReader;
import java.util.Vector;

import de.fhma.ss10.srn.tischbein.core.db.DatabaseException;

/**
 * Kleine Hilfsklasse um die Coderedundanz zu verringern. Ließt einen {@link BufferedReader} zeilenweise aus und
 * übergibt jede Zeile einer Callback-Methode zum Verarbeiten. Das Ergebnis wird in einem Vector mit dem Type
 * <code>T</code> gespeichert.
 * 
 * @author Smolli
 * @param <T>
 *            Jede beliebige Klasse.
 */
abstract class DatabaseTableReader<T> {

    /** Hält den Ergebnisvector. */
    private final Vector<T> list = new Vector<T>();

    /**
     * Standard-Ctor. Startet auch gleichzeitig die Verarbeitung.
     * 
     * @param reader
     *            Ein {@link BufferedReader}-Objekt, das zeilenweise ausgelesen werden soll.
     * @throws DatabaseException
     *             Wird geworfen, wenn der Reader nicht gelesen werden konnte.
     */
    public DatabaseTableReader(final BufferedReader reader) throws DatabaseException {

        try {
            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                this.list.add(this.process(line));
                count++;
            }

            System.out.println(Integer.toString(count) + " Zeilen aus Datei " + reader.toString() + " gelesen.");

            reader.close();
        } catch (Exception e) {
            throw new DatabaseException("Kann die Datei nicht lesen!", e);
        }
    }

    /**
     * Gibt den Ergbenisvektor zurück.
     * 
     * @return Das Ergebnis des Auslesens als {@link Vector}.
     */
    public Vector<T> getResult() {
        return this.list;
    }

    /**
     * Abstrakte Callback-Methode. Sie muss das Ergebnis der Zeile als <code>T</code>-Objekt zurück geben.
     * 
     * @param line
     *            Die zu verarbeitende Zeile
     * @return Das Objekt, das im Ergebnisvektor gespeichert werden soll.
     * @throws Exception
     *             Kann jede beliebige {@link Exception} sein.
     */
    protected abstract T process(final String line) throws Exception;

}

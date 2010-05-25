package de.fhma.ss10.srn.tischbein.core.db;

/**
 * Projekteigene Exception zum Kapseln der einzelnen Exceptions beim Verwenden von Verschlüsselungen.
 * 
 * @author Smolli
 */
public class DatabaseException extends Exception {

    /** Serial UID. */
    private static final long serialVersionUID = -1946758572495693621L;

    /**
     * Standard-Ctor.
     * 
     * @param message
     *            Anzuzeigende Nachricht.
     */
    public DatabaseException(final String message) {
        super(message);
    }

    /**
     * Standard-Ctor mit Exception.
     * 
     * @param message
     *            Anzuzeigende Nachricht.
     * @param e
     *            Verknüpfte Exception.
     */
    public DatabaseException(final String message, final Exception e) {
        super(message, e);
    }

}

package de.fhma.ss10.srn.tischbein.core.db;

/**
 * Exception-Klasse für das User-Objekt.
 * 
 * @author Smolli
 */
public class UserException extends Exception {

    /** Serial UID. */
    private static final long serialVersionUID = 1132743947923286759L;

    /**
     * Standard-Ctor.
     * 
     * @param message
     *            Die Nachricht.
     */
    public UserException(final String message) {
        super(message);
    }

    /**
     * Standard-Ctor mit Exception.
     * 
     * @param message
     *            Die Nachricht.
     * @param exception
     *            Die Exception.
     */
    public UserException(final String message, final Exception exception) {
        super(message, exception);
    }
}

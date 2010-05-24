package de.fhma.ss10.srn.tischbein.core.db;

/**
 * FileItemException.
 * 
 * @author Smolli
 */
public class FileItemException extends Exception {

    /** Serial UID. */
    private static final long serialVersionUID = 2748640808309721869L;

    /**
     * Standrad-Ctor.
     * 
     * @param message
     *            Die Nachricht.
     * @param e
     *            Die Exception.
     */
    public FileItemException(final String message, final Exception e) {
        super(message, e);
    }

}

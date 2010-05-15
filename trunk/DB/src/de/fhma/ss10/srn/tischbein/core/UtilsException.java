package de.fhma.ss10.srn.tischbein.core;

/**
 * Kapselnde Exception um den Wust an möglichen Exceptions in der Utils-Klasse zu mindern.
 * 
 * @author Smolli
 */
public class UtilsException extends Exception {

    /** Serial UID. */
    private static final long serialVersionUID = 4687628517898336248L;

    /**
     * Erstellt eine neue UtilityException.
     * 
     * @param message
     *            Die Nachricht.
     * @param e
     *            Die verursachende Exception.
     */
    public UtilsException(final String message, final Exception e) {
        super(message, e);
    }

}

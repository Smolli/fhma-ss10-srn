package de.fhma.ss10.srn.tischbein.core.crypto;

/**
 * Exception für das crypto-Package.
 * 
 * @author Smolli
 */
public class CryptoException extends Exception {

    /** Serial UID. */
    private static final long serialVersionUID = -2578760367264974049L;

    /**
     * Erstellt eine neue CryptoException.
     * 
     * @param message
     *            Die Nachricht.
     */
    public CryptoException(final String message) {
        super(message);
    }

    /**
     * Erstellt eine neue CryptoException.
     * 
     * @param message
     *            Die Nachricht.
     * @param e
     *            Der Grund.
     */
    public CryptoException(final String message, final Exception e) {
        super(message, e);
    }

}

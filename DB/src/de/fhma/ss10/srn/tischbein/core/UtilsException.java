package de.fhma.ss10.srn.tischbein.core;

public class UtilsException extends Exception {

    /**Serial UID.     */
    private static final long serialVersionUID = 4687628517898336248L;

    public UtilsException(final String message, final Exception e) {
        super(message, e);
    }

}

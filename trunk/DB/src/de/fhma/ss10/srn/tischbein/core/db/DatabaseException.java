package de.fhma.ss10.srn.tischbein.core.db;

/**
 * Projekteigene Exception zum Kapseln der einzelnen Exceptions beim Verwenden von Verschlüsselungen.
 * 
 * @author Smolli
 */
public class DatabaseException extends Exception {

    private static final long serialVersionUID = -1946758572495693621L;

    public DatabaseException(final String string) {
        super(string);
    }

    public DatabaseException(final String string, final Exception e) {
        super(string, e);
    }

}

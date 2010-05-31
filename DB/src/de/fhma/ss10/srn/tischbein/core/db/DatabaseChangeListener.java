package de.fhma.ss10.srn.tischbein.core.db;

/**
 * Einfacher Listener, der über Veränderungen an der Datenbank berichten soll.
 * 
 * @author Smolli
 */
public interface DatabaseChangeListener {

    /**
     * Wird aufgerufen, wenn sich etwas an der Datenbank geändert hat.
     */
    void databaseChanged();

}

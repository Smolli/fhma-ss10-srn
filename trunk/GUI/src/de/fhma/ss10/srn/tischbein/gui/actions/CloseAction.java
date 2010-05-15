package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Programm schließen-Action.
 * 
 * @author Smolli
 */
public final class CloseAction extends AbstractAction {

    /**
     * Listener-Interface für die CloseAction.
     * 
     * @author Smolli
     */
    public interface CloseActionListener {

        /**
         * Schließt die Anwendung.
         */
        void close();

    }

    /** Seruak UID. */
    private static final long serialVersionUID = 8194236130788886964L;

    /** Hält den Listener. */
    private final CloseActionListener listener;

    /**
     * Erstellt eine neue CloseAction.
     * 
     * @param listenerObject
     *            Das Listener-Objeckt.
     */
    public CloseAction(final CloseActionListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        this.listener.close();
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Beenden";
        } else {
            return super.getValue(key);
        }
    }

}

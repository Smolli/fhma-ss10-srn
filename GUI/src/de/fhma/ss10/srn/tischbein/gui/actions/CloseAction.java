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
    public interface CloseActionParent {

        /**
         * Schließt die Anwendung.
         */
        void closeFrame();

    }

    /** Seruak UID. */
    private static final long serialVersionUID = 8194236130788886964L;

    /** Hält den Listener. */
    private final CloseActionParent listener;

    /**
     * Erstellt eine neue CloseAction.
     * 
     * @param listenerObject
     *            Das Listener-Objeckt.
     */
    public CloseAction(final CloseActionParent listenerObject) {
        super();

        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        this.listener.closeFrame();
    }

    @Override
    public Object getValue(final String key) {
        Object result;

        if (key.equals(Action.NAME)) {
            result = "Beenden";
        } else {
            result = super.getValue(key);
        }

        return result;
    }

}

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

    public interface CloseActionListener {

        void close();

    }

    /** Seruak UID. */
    private static final long serialVersionUID = 8194236130788886964L;
    private final CloseActionListener listener;

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

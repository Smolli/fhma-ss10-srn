package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Action zum Ausloggen eines Benutzers.
 * 
 * @author Smolli
 */
public final class LogoutAction extends AbstractAction {

    public interface LogoutActionListener {

        void logout();

    }

    /** Serial UID. */
    private static final long serialVersionUID = -177255198567493408L;
    private final LogoutActionListener listener;

    public LogoutAction(final LogoutActionListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        this.listener.logout();
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Logout";
        } else {
            return super.getValue(key);
        }
    }

}

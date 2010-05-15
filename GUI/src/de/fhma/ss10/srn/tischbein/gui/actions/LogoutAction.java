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

    /** Serial UID. */
    private static final long serialVersionUID = -177255198567493408L;

    @Override
    public void actionPerformed(final ActionEvent arg0) {
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

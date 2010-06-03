package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.gui.frames.WorkFrameBaseParent;

/**
 * Action zum Ausloggen eines Benutzers.
 * 
 * @author Smolli
 */
public final class LogoutAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = -177255198567493408L;

    /** Hält den Listener. */
    private final WorkFrameBaseParent listener;

    /**
     * Erstellt eine neue LogoutAction.
     * 
     * @param listenerObject
     *            Das Listener-Objekt.
     */
    public LogoutAction(final WorkFrameBaseParent listenerObject) {
        super();

        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        this.listener.logout();
    }

    @Override
    public Object getValue(final String key) {
        Object result;

        if (key.equals(Action.NAME)) {
            result = "Logout";
        } else {
            result = super.getValue(key);
        }

        return result;
    }

}

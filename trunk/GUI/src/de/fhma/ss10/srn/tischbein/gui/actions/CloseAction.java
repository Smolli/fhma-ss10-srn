package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

/**
 * Programm schließen-Action.
 * 
 * @author Smolli
 */
public final class CloseAction extends AbstractAction {

    /** Seruak UID. */
    private static final long serialVersionUID = 8194236130788886964L;

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        Launcher.getFrame().close();
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

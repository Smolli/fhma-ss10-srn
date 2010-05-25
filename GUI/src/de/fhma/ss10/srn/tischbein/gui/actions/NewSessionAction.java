package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.gui.frames.LoginFrame;

/**
 * Öffnet eine neue Session.
 * 
 * @author Smolli
 */
public final class NewSessionAction extends AbstractAction implements Action {

    /** Serial UID. */
    private static final long serialVersionUID = 9006740500253306285L;

    @Override
    public void actionPerformed(final ActionEvent e) {
        new LoginFrame();
    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Neue Session...";
        } else {
            return super.getValue(key);
        }
    }

}

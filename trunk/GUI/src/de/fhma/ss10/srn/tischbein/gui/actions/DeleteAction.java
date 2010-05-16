package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Delete-Action zum löschen einer eigenen Datei.
 * 
 * @author Smolli
 */
public final class DeleteAction extends AbstractAction implements Action {

    /** Serial UID. */
    private static final long serialVersionUID = 1042611735666746461L;

    @Override
    public void actionPerformed(final ActionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Löschen";
        } else {
            return super.getValue(key);
        }
    }

}

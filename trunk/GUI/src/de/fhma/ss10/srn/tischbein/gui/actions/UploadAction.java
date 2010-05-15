package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Upload-Action.
 * 
 * @author Smolli
 */
public final class UploadAction extends AbstractAction {

    /** Serial UID. */
    private static final long serialVersionUID = 7077305932498378892L;

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getValue(final String key) {
        if (key.equals(Action.NAME)) {
            return "Hochladen";
        } else {
            return super.getValue(key);
        }
    }

}

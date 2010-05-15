package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public final class DeleteAction extends AbstractAction implements Action {

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

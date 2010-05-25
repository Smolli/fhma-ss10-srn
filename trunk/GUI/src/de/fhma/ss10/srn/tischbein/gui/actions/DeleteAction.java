package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.Database;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;

/**
 * Delete-Action zum löschen einer eigenen Datei.
 * 
 * @author Smolli
 */
public final class DeleteAction extends AbstractAction implements Action {

    public interface DeleteActionListener {

        User getCurrentUser();

        FileItem getSelectedFile();

        void notifyChange();

    }

    /** Serial UID. */
    private static final long serialVersionUID = 1042611735666746461L;
    private final DeleteActionListener parent;

    public DeleteAction(final DeleteActionListener parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        FileItem item = this.parent.getSelectedFile();
        User user = this.parent.getCurrentUser();

        if (item.getOwner() != user) {
            return;
        }

        try {
            Database.getInstance().deleteFileItem(item);

            this.parent.notifyChange();
        } catch (Exception ex) {
            GuiUtils.displayError("Kann die Datei nicht löschen!", ex);
        }
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

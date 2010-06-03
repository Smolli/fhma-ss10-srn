package de.fhma.ss10.srn.tischbein.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.frames.WorkFrameBaseParent;

/**
 * Delete-Action zum löschen einer eigenen Datei.
 * 
 * @author Smolli
 */
public final class DeleteAction extends AbstractAction implements Action {

    //    /**
    //     * Hilfslistener um an die Formulardaten zu kommen.
    //     * 
    //     * @author Smolli
    //     */
    //    public interface DeleteActionParent {
    //
    //        /**
    //         * Gibt den aktuell eingeloggten Benutzer zurück.
    //         * 
    //         * @return Der {@link User}.
    //         */
    //        User getCurrentUser();
    //
    //        /**
    //         * Gibt die aktuell ausgewählte Datei zurück.
    //         * 
    //         * @return Das {@link FileItem}.
    //         */
    //        FileItem getSelectedFile();
    //
    //    }

    /** Serial UID. */
    private static final long serialVersionUID = 1042611735666746461L;
    /** Hält den Daten-Listener. */
    private final WorkFrameBaseParent parent;

    /**
     * Standard-Ctor.
     * 
     * @param listenerObject
     *            Das 'Eltern'-Element, aus dem die Daten geholt werden sollen.
     */
    public DeleteAction(final WorkFrameBaseParent listenerObject) {
        super();

        this.parent = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final FileItem item = this.parent.getSelectedFile();
        final User user = this.parent.getCurrentUser();

        if (item.getOwner() == user) {
            try {
                Database.getInstance().deleteFileItem(item);

                this.parent.selectFile(null, null, 0);

                //            this.listener.notifyChange();
            } catch (final Exception ex) {
                GuiUtils.displayError("Kann die Datei nicht löschen!", ex);
            }
        } else {
            GuiUtils.display("Die Datei muss schon dir gehören!");
        }
    }

    @Override
    public Object getValue(final String key) {
        Object result;

        if (key.equals(Action.NAME)) {
            result = "Löschen";
        } else {
            result = super.getValue(key);
        }

        return result;
    }

}

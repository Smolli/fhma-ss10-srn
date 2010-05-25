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

    /**
     * Hilfslistener um an die Formulardaten zu kommen.
     * 
     * @author Smolli
     */
    public interface DeleteActionListener {

        /**
         * Gibt den aktuell eingeloggten Benutzer zurück.
         * 
         * @return Der {@link User}.
         */
        User getCurrentUser();

        /**
         * Gibt die aktuell ausgewählte Datei zurück.
         * 
         * @return Das {@link FileItem}.
         */
        FileItem getSelectedFile();

        /**
         * Wird aufgerufen, wenn sich an den Listen des Benutzers etwas geändert hat.
         */
        void notifyChange();

    }

    /** Serial UID. */
    private static final long serialVersionUID = 1042611735666746461L;
    /** Hält den Daten-Listener. */
    private final DeleteActionListener listener;

    /**
     * Standard-Ctor.
     * 
     * @param listenerObject
     *            Das 'Eltern'-Element, aus dem die Daten geholt werden sollen.
     */
    public DeleteAction(final DeleteActionListener listenerObject) {
        this.listener = listenerObject;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        FileItem item = this.listener.getSelectedFile();
        User user = this.listener.getCurrentUser();

        if (item.getOwner() != user) {
            return;
        }

        try {
            Database.getInstance().deleteFileItem(item);

            this.listener.notifyChange();
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

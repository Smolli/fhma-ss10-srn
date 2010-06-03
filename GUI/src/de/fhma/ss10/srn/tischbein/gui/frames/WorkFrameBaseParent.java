package de.fhma.ss10.srn.tischbein.gui.frames;

import javax.swing.JList;

import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionParent;

/**
 * Hilfsinterface um alle Events an das {@link WorkFrame} weiterzugeben oder an Inhalte heranzukommen.
 * 
 * @author Smolli
 */
public interface WorkFrameBaseParent extends CloseActionParent {

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
     * Loggt den Benutzer aus.
     */
    void logout();

    /**
     * Wird aufgerufen, wenn eine Datei in der Liste ausgewählt wurde.
     * 
     * @param fileItem
     *            Das {@link FileItem}, das ausgewählt wurde.
     * @param list
     *            Die {@link JList}, in der die Datei ausgewählt wurde.
     * @param index
     *            Der Index des Elements in der Liste.
     */
    void selectFile(FileItem fileItem, JList list, int index);

}

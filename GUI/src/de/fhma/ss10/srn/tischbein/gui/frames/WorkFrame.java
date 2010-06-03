package de.fhma.ss10.srn.tischbein.gui.frames;

import javax.swing.JList;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.forms.WorkForm;
import de.fhma.ss10.srn.tischbein.gui.launcher.Launcher;

/**
 * Arbeitsfenster.
 * 
 * @author Smolli
 */
public final class WorkFrame extends AbstractWorkFrameBase {

    /** Serial UID. */
    private static final long serialVersionUID = -5369888389274792872L;
    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(WorkForm.class);

    /**
     * Erstellt ein neues Arbeitsfenster mit dem übergebenen Benutzer.
     * 
     * @param user
     *            Der Benutzer.
     */
    public WorkFrame(final User user) {
        super(user);

        this.setTitle(Launcher.PRODUCT_NAME + " - " + user.getName());
    }

    @Override
    public void closeFrame() {
        this.dispose();
    }

    @Override
    public void databaseChanged() {
        this.updateLists();

        if (this.getLastList() != null) {
            this.getLastList().setSelectedIndex(this.getLastIndex());
        }
    }

    @Override
    public void dispose() {
        Database.getInstance();
        Database.removeChangeListener(this);

        super.dispose();
    }

    @Override
    public void logout() {
        Database.getInstance().lock(this.getCurrentUser());

        new LoginFrame();

        this.closeFrame();
    }

    /**
     * Wird aufgerufen, wenn der Benutzer entweder in die {@link WorkForm#userFilesList} oder
     * {@link WorkForm#otherFilesList} geklickt hat.
     * 
     * @param file
     *            Die Datei, die der Benutzer ausgewählt hat.
     * @param sender
     *            Die {@link JList}, aus der die Datei ausgewählt wurde.
     * @param index
     *            Der Index, mit dem die Auswahl in der {@link JList} verbunden ist.
     */
    public void selectFile(final FileItem file, final JList sender, final int index) {
        try {
            this.setSelectedFile(file);

            this.setLastSelection(sender, index);

            WorkFrame.LOG.debug(this.getSelectedFile() + " wurde ausgewählt");
        } catch (final Exception e) {
            GuiUtils.displayError("Datei kann nicht angezeigt werden!", e);
        }
    }

}

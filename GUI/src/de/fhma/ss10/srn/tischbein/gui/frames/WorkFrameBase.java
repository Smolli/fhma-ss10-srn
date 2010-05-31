package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import de.fhma.ss10.srn.tischbein.core.db.DatabaseChangeListener;
import de.fhma.ss10.srn.tischbein.core.db.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.FileItemException;
import de.fhma.ss10.srn.tischbein.core.db.User;
import de.fhma.ss10.srn.tischbein.core.db.UserDescriptor;
import de.fhma.ss10.srn.tischbein.gui.GuiUtils;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction.CloseActionParent;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction.DeleteActionParent;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction.LogoutActionParent;
import de.fhma.ss10.srn.tischbein.gui.forms.WorkForm;
import de.fhma.ss10.srn.tischbein.gui.frames.AccessTableModel.AccessTableModelParent;
import de.fhma.ss10.srn.tischbein.gui.frames.FilesSelectionModel.FileSelectionModelParent;
import de.fhma.ss10.srn.tischbein.gui.frames.UploadFrame.UploadFrameListener;

/**
 * Basisklasse für das WorkFrame.
 * 
 * @author Smolli
 */
public class WorkFrameBase extends WorkForm implements CloseActionParent, LogoutActionParent, UploadFrameListener,
        DeleteActionParent, DatabaseChangeListener, FileSelectionModelParent, AccessTableModelParent {

    /** Serial UID. */
    private static final long serialVersionUID = 7264486985719383548L;
    /** Hält den Logger. */
    private static final Logger LOG = Logger.getLogger(WorkFrameBase.class);
    /** Hält die {@link JList}, aus der die aktuelle Datei ausgewählt ist. */
    private JList lastList;
    /** Hält den Index in der JList von der aktelle ausgewählten Datei. */
    private int lastIndex;
    /** Hält den eingeloggten Benutzer. */
    private transient User currentUser;
    /** Hält die momentan ausgewählte Datei. */
    private transient FileItem selectedFile = null;

    @Override
    public final void closeFrame() {
        //        Database.getInstance().shutdown();

        this.dispose();
    }

    @Override
    public final void databaseChanged() {
        this.updateLists();

        if (this.lastList != null) {
            this.lastList.setSelectedIndex(this.lastIndex);
        }
    }

    @Override
    public final User getCurrentUser() {
        return this.currentUser;
    }

    @Override
    public final FileItem getSelectedFile() {
        return this.selectedFile;
    }

    @Override
    public final void logout() {
        this.getCurrentUser().lock();

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
    public final void selectFile(final FileItem file, final JList sender, final int index) {
        try {
            this.selectedFile = file;

            this.updateFileView();

            this.updateListSelection();

            this.updateAccessTable();

            this.lastList = sender;
            this.lastIndex = index;

            WorkFrameBase.LOG.debug(this.selectedFile + " wurde ausgewählt");
        } catch (final Exception e) {
            GuiUtils.displayError("Datei kann nicht angezeigt werden!", e);
        }
    }

    /**
     * Setzt den ausgewählten Benutzer.
     * 
     * @param user
     *            Der Bentutzer.
     */
    public final void setCurrentUser(final User user) {
        this.currentUser = user;
    }

    /**
     * Erneuert die beiden Dateilisten.
     */
    protected final void updateLists() {
        final UserDescriptor descriptor = this.getCurrentUser().getDescriptor();

        this.updateList(this.userFilesList, descriptor.getFileList());

        this.updateList(this.otherFilesList, descriptor.getAccessList());
    }

    /**
     * Setzt die Access-Table-Ansicht.
     */
    private void updateAccessTable() {
        if ((this.selectedFile != null) && (this.selectedFile.getOwner() == this.getCurrentUser())) {
            this.accessTable.setVisible(true);
            this.accessTable.repaint();
        } else {
            this.accessTable.setVisible(false);
        }
    }

    /**
     * Setzt die FileView entsprechende der übergebenen Datei.
     * 
     * @throws FileItemException
     *             Wird geworfen, wenn die Datei nicht geladen werden kann.
     */
    private void updateFileView() throws FileItemException {
        if (this.selectedFile == null) {
            ((TitledBorder) this.viewPanel.getBorder()).setTitle("");
        } else {
            final byte[] content = this.selectedFile.getContent();

            this.fileView.setText(new String(content));
            this.fileView.setCaretPosition(0);

            ((TitledBorder) this.viewPanel.getBorder()).setTitle(this.selectedFile.getName());
        }

        this.viewPanel.doLayout();
        this.viewPanel.repaint();
    }

    /**
     * Erneuert eine spezifische Dateiliste.
     * 
     * @param list
     *            Die {@link JList}, die erneuert werden soll.
     * @param fileList
     *            Die Elemente, die in die Liste geschrieben werden sollen.
     */
    private void updateList(final JList list, final List<FileItem> fileList) {
        final DefaultListModel model = (DefaultListModel) list.getModel();

        model.clear();

        for (final FileItem file : fileList) {
            model.addElement(file);
        }

        list.repaint();
    }

    /**
     * Löscht die Selektion einer der beiden {@link JList}, damit eindeutig ist, aus welcher die aktuell ausgewählte
     * Datei stammt.
     */
    private void updateListSelection() {
        if (this.selectedFile.getOwner() == this.getCurrentUser()) {
            this.otherFilesList.clearSelection();
        } else {
            this.userFilesList.clearSelection();
        }
    }

}

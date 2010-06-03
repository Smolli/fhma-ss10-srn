package de.fhma.ss10.srn.tischbein.gui.frames;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.border.TitledBorder;

import de.fhma.ss10.srn.tischbein.core.db.dbms.Database;
import de.fhma.ss10.srn.tischbein.core.db.dbms.DatabaseChangeListener;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItem;
import de.fhma.ss10.srn.tischbein.core.db.fileitem.FileItemException;
import de.fhma.ss10.srn.tischbein.core.db.user.User;
import de.fhma.ss10.srn.tischbein.core.db.user.UserDescriptor;
import de.fhma.ss10.srn.tischbein.gui.actions.CloseAction;
import de.fhma.ss10.srn.tischbein.gui.actions.DeleteAction;
import de.fhma.ss10.srn.tischbein.gui.actions.LogoutAction;
import de.fhma.ss10.srn.tischbein.gui.actions.NewSessionAction;
import de.fhma.ss10.srn.tischbein.gui.actions.UploadAction;
import de.fhma.ss10.srn.tischbein.gui.forms.WorkForm;

/**
 * Basisklasse für das WorkFrame.
 * 
 * @author Smolli
 */
public abstract class AbstractWorkFrameBase extends WorkForm implements DatabaseChangeListener, WorkFrameBaseParent {

    /** Serial UID. */
    private static final long serialVersionUID = 7264486985719383548L;
    /** Hält die {@link JList}, aus der die aktuelle Datei ausgewählt ist. */
    private JList lastList;
    /** Hält den Index in der JList von der aktelle ausgewählten Datei. */
    private int lastIndex;
    /** Hält den eingeloggten Benutzer. */
    private final transient User currentUser;
    /** Hält die momentan ausgewählte Datei. */
    private transient FileItem selectedFile = null;

    /**
     * Ctor.
     * 
     * @param user
     *            Der Benutzer.
     */
    protected AbstractWorkFrameBase(final User user) {
        super();

        this.setupActions();

        this.currentUser = user;

        Database.addChangeListener(this);

        this.initLists();

        this.hintTextFileView.setVisible(true);
        this.fileViewPane.setVisible(false);

        this.setVisible(true);
    }

    @Override
    public final User getCurrentUser() {
        return this.currentUser;
    }

    @Override
    public final FileItem getSelectedFile() {
        return this.selectedFile;
    }

    protected int getLastIndex() {
        return this.lastIndex;
    }

    protected JList getLastList() {
        return this.lastList;
    }

    protected void setLastSelection(final JList list, final int index) {
        this.lastList = list;
        this.lastIndex = index;
    }

    protected void setSelectedFile(final FileItem selectedFile) throws FileItemException {
        this.selectedFile = selectedFile;

        this.updateFileView();

        this.updateListSelection();

        this.updateAccessTable();
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
     * Initialisiert die drei GUI-Listen.
     */
    private void initLists() {
        this.userFilesList.setSelectionModel(new FilesSelectionModel(this, this.userFilesList));
        this.otherFilesList.setSelectionModel(new FilesSelectionModel(this, this.otherFilesList));
        this.accessTable.setModel(new AccessTableModel(this));

        this.userFilesList.setModel(new DefaultListModel());

        this.accessTable.setDefaultRenderer(String.class, new LendTableRenderer());

        this.setAccessTableHintText(true);

        this.otherFilesList.setModel(new DefaultListModel());

        this.updateLists();
    }

    private void setAccessTableHintText(final boolean show) {
        if (show) {
            this.accessPane.setVisible(false);
            this.hintTextAccessTable.setVisible(true);
        } else {
            this.hintTextAccessTable.setVisible(false);
            this.accessPane.setVisible(true);
        }

        this.accessPanel.repaint();
    }

    /**
     * Vergibt die Actions an die GUI-Elemente.
     */
    private void setupActions() {
        this.closeButton.setAction(new CloseAction(this));
        this.logoutButton.setAction(new LogoutAction(this));
        this.uploadButton.setAction(new UploadAction(this));
        this.deleteButton.setAction(new DeleteAction(this));
        this.newsessionButton.setAction(new NewSessionAction());
    }

    /**
     * Setzt die Access-Table-Ansicht.
     */
    private void updateAccessTable() {
        if ((this.getSelectedFile() != null) && (this.getSelectedFile().getOwner() == this.getCurrentUser())) {
            final AccessTableModel model = (AccessTableModel) this.accessTable.getModel();

            model.updateData();

            this.accessTable.repaint();

            this.setAccessTableHintText(false);
        } else {
            this.setAccessTableHintText(true);
        }
    }

    /**
     * Setzt die FileView entsprechende der übergebenen Datei.
     * 
     * @throws FileItemException
     *             Wird geworfen, wenn die Datei nicht geladen werden kann.
     */
    private void updateFileView() throws FileItemException {
        if (this.getSelectedFile() == null) {
            ((TitledBorder) this.viewPanel.getBorder()).setTitle("");

            this.hintTextFileView.setVisible(true);
            this.fileViewPane.setVisible(false);
        } else {
            this.hintTextFileView.setVisible(false);
            this.fileViewPane.setVisible(true);

            final byte[] content = this.getSelectedFile().getContent();

            this.fileView.setText(new String(content));
            this.fileView.setCaretPosition(0);

            ((TitledBorder) this.viewPanel.getBorder()).setTitle(this.getSelectedFile().getName());
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
        if (this.getSelectedFile() != null) {
            if (this.getSelectedFile().getOwner() == this.getCurrentUser()) {
                this.otherFilesList.clearSelection();
            } else {
                this.userFilesList.clearSelection();
            }
        }
    }

}
